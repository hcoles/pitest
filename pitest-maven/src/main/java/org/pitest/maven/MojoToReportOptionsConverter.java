/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Build;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.DefaultDependencyNode;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.DirectoryClassPathRoot;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.config.ExecutionMode;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;
import org.pitest.util.Verbosity;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.pitest.functional.Streams.asStream;

public class MojoToReportOptionsConverter {

  private final PitMojo mojo;
  private final Predicate<Artifact>     dependencyFilter;
  private final Log                     log;
  private final SurefireConfigConverter surefireConverter;

  public MojoToReportOptionsConverter(final PitMojo mojo,
      SurefireConfigConverter surefireConverter,
      Predicate<Artifact> dependencyFilter) {
    this.mojo = mojo;
    this.dependencyFilter = dependencyFilter;
    this.log = mojo.getLog();
    this.surefireConverter = surefireConverter;
  }

  public ReportOptions convert() {

    final List<String> classPath = new ArrayList<>();

    try {
      classPath.addAll(this.mojo.getProject().getTestClasspathElements());
    } catch (final DependencyResolutionRequiredException e1) {
      this.log.info(e1);
    }

    addOwnDependenciesToClassPath(classPath);

    classPath.addAll(this.mojo.getAdditionalClasspathElements());

    autoAddJUnitPlatformLauncher(classPath);
    autoAddJUnitPlatformEngine(classPath);
    autoAddJupiterEngine(classPath);
    removeExcludedDependencies(classPath);

    addCrossModuleDirsToClasspath(classPath);

    ReportOptions option = parseReportOptions(classPath);
    ReportOptions withSureFire = updateFromSurefire(option);

    // Null check here is a bad unit testing artifact, should never be null in real life
    ReportOptions effective = withSureFire != null ? withSureFire : option;

    // argline may contain surefire style properties that require expanding
    if (effective.getArgLine() != null) {
      log.info("Replacing properties in argLine " + effective.getArgLine());
      effective.setArgLine(this.replacePropertyExpressions(effective.getArgLine()));
    }
    return effective;

  }

  private void addCrossModuleDirsToClasspath(List<String> classPath) {
    // Add the output directories modules we depend on to the start of the classpath.
    // If we resolve cross project classes from a jar, the path match
    // will fail. This is only an issue when running the pitest goal directly.
    if (mojo.isCrossModule()) {
      classPath.addAll(0, crossModuleDependencies());
    }
  }

  /**
   * The junit 5 plugin needs junit-platform-launcher to run, but this will not be on the classpath
   * of the project. We want to use the same version that surefire (and therefore the SUT) uses, not
   * the one the plugin was built against.
   * <p>
   * It is not declared as a normal dependency, instead surefire picks the version to use based on
   * other junit jars on the classpath. We're forced to do something similar here.
   *
   * @param classPath classpath to modify
   */
  private void autoAddJUnitPlatformLauncher(List<String> classPath) {
    autoAddJUnitPlatformArtifact(classPath, "junit-platform-launcher");
  }

  private void autoAddJUnitPlatformEngine(List<String> classPath) {
    autoAddJUnitPlatformArtifact(classPath, "junit-platform-engine");
  }


  private void autoAddJUnitPlatformArtifact(List<String> classPath, String artifactId) {
    List<Artifact> junitDependencies = this.mojo.getProject().getArtifacts().stream()
            .filter(a -> a.getGroupId().equals("org.junit.platform"))
            .collect(Collectors.toList());

    // If the artifact has been manually added to the dependencies, there is nothing to do
    if (junitDependencies.stream().anyMatch(a -> a.getArtifactId().equals(artifactId))) {
      return;
    }

    Optional<Artifact> maybeJUnitPlatform = findJUnitArtifact(junitDependencies);
    if (maybeJUnitPlatform.isEmpty()) {
      this.log.debug("JUnit 5 not on classpath");
      return;
    }

    // Look for platform engine or platform commons on classpath
    Artifact toMatch = maybeJUnitPlatform.get();

    // Assume that artifact has been released with same version number as engine and commons
    DefaultArtifact platformLauncher = new DefaultArtifact(toMatch.getGroupId(), artifactId, "jar",
            toMatch.getVersion());

    addArtifact(classPath, platformLauncher);
  }

  private void autoAddJupiterEngine(List<String> classPath) {
    List<Artifact> junitDependencies = this.mojo.getProject().getArtifacts().stream()
            .filter(a -> a.getGroupId().equals("org.junit.jupiter"))
            .collect(Collectors.toList());

    // If the engine has been manually added to the dependencies, there is nothing to do
    if (junitDependencies.stream().anyMatch(a -> a.getArtifactId().equals("junit-jupiter-engine"))) {
      return;
    }

    Optional<Artifact> maybeApi = this.mojo.getProject().getArtifacts().stream()
            .filter(a -> a.getArtifactId().equals("junit-jupiter-api"))
            .findFirst();

    if (maybeApi.isEmpty()) {
      return;
    }

    // Look for platform engine or platform commons on classpath
    Artifact toMatch = maybeApi.get();

    // Assume that engine has been released with same version number as api
    DefaultArtifact platformLauncher = new DefaultArtifact(toMatch.getGroupId(), "junit-jupiter-engine", "jar",
            toMatch.getVersion());

    addArtifact(classPath, platformLauncher);

  }

  private void addArtifact(List<String> classPath, DefaultArtifact platformLauncher) {
    try {

      ArtifactRequest r = new ArtifactRequest();
      r.setArtifact(platformLauncher);

      r.setRepositories(this.mojo.getProject().getRemotePluginRepositories());
      ArtifactResult resolved = this.mojo.repositorySystem().resolveArtifact(mojo.session().getRepositorySession(), r);

      this.log.info("Auto adding " + resolved + " to classpath.");
      classPath.add(resolved.getArtifact().getFile().getAbsolutePath());

      // get any transitive dependencies,
      // although this doesn't seem to be neccesary for current releases of junit
      DependencyRequest dependencyRequest = new DependencyRequest(new DefaultDependencyNode(platformLauncher), null);
      DependencyResult transitive = this.mojo.repositorySystem().resolveDependencies(mojo.session().getRepositorySession(), dependencyRequest);
      for (ArtifactResult result : transitive.getArtifactResults()) {
        this.log.info("Auto adding " + result + " to classpath.");
        classPath.add(result.getArtifact().getFile().getAbsolutePath());
      }

    } catch (DependencyResolutionException | ArtifactResolutionException e) {
      this.log.error("Could not resolve " + platformLauncher);
      throw new RuntimeException(e);
    }
  }

  private static Optional<Artifact> findJUnitArtifact(List<Artifact> junitDependencies) {
    Optional<Artifact> maybeEngine = junitDependencies.stream()
            .filter(a -> a.getArtifactId().equals("junit-platform-engine"))
            .findAny();
    if (maybeEngine.isPresent()) {
      return maybeEngine;
    }

    return junitDependencies.stream()
            .filter(a -> a.getArtifactId().equals("junit-platform-commons"))
            .findAny();
  }

  private void removeExcludedDependencies(List<String> classPath) {
    for (Object artifact : this.mojo.getProject().getArtifacts()) {
      final Artifact dependency = (Artifact) artifact;
      if (this.mojo.getClasspathDependencyExcludes().contains(
          dependency.getGroupId() + ":" + dependency.getArtifactId())) {
        classPath.remove(dependency.getFile().getPath());
      }
    }
  }

  private ReportOptions parseReportOptions(final List<String> classPath) {
    final ReportOptions data = new ReportOptions();

    if (this.mojo.getProject().getBuild() != null) {

      List<String> codePaths = new ArrayList<>();
      codePaths.add(this.mojo.getProject().getBuild()
              .getOutputDirectory());

      if (mojo.isCrossModule()) {
        codePaths.addAll(crossModuleDependencies());
      }

      this.log.info("Mutating from "
              + String.join(",", codePaths));

      data.setCodePaths(Collections.singleton(this.mojo.getProject().getBuild()
          .getOutputDirectory()));

      data.setCodePaths(codePaths);
    }

    data.setClassPathElements(classPath);

    data.setFailWhenNoMutations(shouldFailWhenNoMutations());

    data.setTargetClasses(determineTargetClasses());
    data.setTargetTests(determineTargetTests());

    data.setExcludedMethods(this.mojo
        .getExcludedMethods());
    data.setExcludedClasses(this.mojo.getExcludedClasses());
    data.setExcludedTestClasses(globStringsToPredicates(this.mojo
        .getExcludedTestClasses()));
    data.setNumberOfThreads(this.mojo.getThreads());
    data.setExcludedRunners(this.mojo.getExcludedRunners());

    data.setReportDir(this.mojo.getReportsDirectory().getAbsolutePath());
    configureVerbosity(data);
    if (this.mojo.getJvmArgs() != null) {
      data.addChildJVMArgs(this.mojo.getJvmArgs());
    }
    if (this.mojo.getArgLine() != null) {
      data.setArgLine(this.mojo.getArgLine());
    }

    data.setMutators(determineMutators());
    data.setFeatures(determineFeatures());
    data.setTimeoutConstant(this.mojo.getTimeoutConstant());
    data.setTimeoutFactor(this.mojo.getTimeoutFactor());
    if (hasValue(this.mojo.getAvoidCallsTo())) {
      data.setLoggingClasses(this.mojo.getAvoidCallsTo());
    }

    final List<String> sourceRoots = determineSourceRoots();

    data.setSourceDirs(stringsToPaths(sourceRoots));

    data.addOutputFormats(determineOutputFormats());

    setTestGroups(data);

    data.setFullMutationMatrix(this.mojo.isFullMutationMatrix());

    data.setMutationUnitSize(this.mojo.getMutationUnitSize());
    data.setShouldCreateTimestampedReports(this.mojo.isTimestampedReports());
    data.setDetectInlinedCode(this.mojo.isDetectInlinedCode());

    determineHistory(data);
    
    data.setExportLineCoverage(this.mojo.isExportLineCoverage());
    data.setMutationEngine(this.mojo.getMutationEngine());
    data.setJavaExecutable(this.mojo.getJavaExecutable());
    data.setFreeFormProperties(createPluginProperties());
    data.setIncludedTestMethods(this.mojo.getIncludedTestMethods());

    data.setSkipFailingTests(this.mojo.skipFailingTests());

    data.setInputEncoding(this.mojo.getSourceEncoding());
    data.setOutputEncoding(this.mojo.getOutputEncoding());

    if (this.mojo.getProjectBase() != null) {
      data.setProjectBase(FileSystems.getDefault().getPath(this.mojo.getProjectBase()));
    }

    if (this.mojo.isDryRun()) {
      data.setExecutionMode(ExecutionMode.DRY_RUN);
    }

    checkForObsoleteOptions(this.mojo);

    return data;
  }

  private List<String> determineSourceRoots() {
    final List<String> sourceRoots = new ArrayList<>();
    sourceRoots.addAll(this.mojo.getProject().getCompileSourceRoots());
    sourceRoots.addAll(this.mojo.getProject().getTestCompileSourceRoots());
    if (mojo.isCrossModule()) {
      List<String> otherRoots = dependedOnProjects().stream()
              .flatMap(p -> p.getCompileSourceRoots().stream())
              .collect(Collectors.toList());

      sourceRoots.addAll(otherRoots);
    }
    return sourceRoots;
  }

  private Collection<String> crossModuleDependencies() {
    return dependedOnProjects().stream()
            .map(MavenProject::getBuild)
            .map(Build::getOutputDirectory)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
  }

  private List<MavenProject> dependedOnProjects() {
    // strip version from dependencies
    Set<String> inScope = this.mojo.getProject().getDependencies().stream()
            .map(p -> p.getGroupId() + ":" + p.getArtifactId())
            .collect(Collectors.toSet());


    return this.mojo.allProjects().stream()
            .filter(p -> inScope.contains(p.getGroupId() + ":" + p.getArtifactId()))
            .collect(Collectors.toList());

  }

  private void configureVerbosity(ReportOptions data) {
    if (this.mojo.isVerbose()) {
      data.setVerbosity(Verbosity.VERBOSE);
    } else {
      Verbosity v = Verbosity.fromString(mojo.getVerbosity());
      data.setVerbosity(v);
    }

  }

  private void checkForObsoleteOptions(PitMojo mojo) {
    if (mojo.getMaxMutationsPerClass() > 0) {
      throw new IllegalArgumentException("The max mutations per class argument is no longer supported, "
              + "use features=+CLASSLIMIT(limit[" + mojo.getMaxMutationsPerClass() + "]) instead");
    }
  }

  private void determineHistory(final ReportOptions data) {

    // set explicit history files if configured
    data.setHistoryInputLocation(this.mojo.getHistoryInputFile());
    data.setHistoryOutputLocation(this.mojo.getHistoryOutputFile());

    // If withHistory option set, overwrite config with files in temp dir.
    // This allows a user to configure files for use on ci, but still easily use temp files
    // for local running
    if (this.mojo.useHistory()) {
      useHistoryFileInTempDir(data);
    }

    if (data.getHistoryInputLocation() != null) {
      log.info("Will read history at " + data.getHistoryInputLocation());
    }

    if (data.getHistoryOutputLocation() != null) {
      log.info("Will write history at " + data.getHistoryOutputLocation());
    }
  }

  private void useHistoryFileInTempDir(final ReportOptions data) {
    String tempDir = System.getProperty("java.io.tmpdir");
    MavenProject project = this.mojo.getProject();
    String name = project.getGroupId() + "."
        + project.getArtifactId() + "."
        + project.getVersion() + "_pitest_history.bin";
    File historyFile = new File(tempDir, name);

    if (mojo.getHistoryInputFile() != null || mojo.getHistoryOutputFile() != null) {
      log.info("Using withHistory option. This overrides the explicitly set history file paths.");
    }
    data.setHistoryInputLocation(historyFile);
    data.setHistoryOutputLocation(historyFile);

  }


  private ReportOptions updateFromSurefire(ReportOptions option) {
    Collection<Plugin> plugins = lookupPlugin("org.apache.maven.plugins:maven-surefire-plugin");
    if (!this.mojo.isParseSurefireConfig()) {
      return option;
    } else if (plugins.isEmpty()) {
      this.log.warn("Could not find surefire configuration in pom");
      return option;
    }

    Plugin surefire = plugins.iterator().next();
    if (surefire != null) {
      return this.surefireConverter.update(option,
          (Xpp3Dom) surefire.getConfiguration());
    } else {
      return option;
    }

  }

  private Collection<Plugin> lookupPlugin(String key) {
    List<Plugin> plugins = this.mojo.getProject().getBuildPlugins();
    return FCollection.filter(plugins, hasKey(key));
  }

  private static Predicate<Plugin> hasKey(final String key) {
    return a -> a.getKey().equals(key);
  }

  private boolean shouldFailWhenNoMutations() {
    return this.mojo.isFailWhenNoMutations();
  }

  private void setTestGroups(final ReportOptions data) {
    final TestGroupConfig conf = new TestGroupConfig(
        this.mojo.getExcludedGroups(), this.mojo.getIncludedGroups());
    data.setGroupConfig(conf);
  }

  private void addOwnDependenciesToClassPath(final List<String> classPath) {
    for (final Artifact dependency : filteredDependencies()) {
      this.log.info("Adding " + dependency.getGroupId() + ":"
          + dependency.getArtifactId() + " to SUT classpath");
      classPath.add(dependency.getFile().getAbsolutePath());
    }

    // If the user as explicitly added junit platform classes to the pitest classpath, trust that they
    // know what they're doing and add them in
    this.mojo.getPluginArtifactMap().values().stream().filter(a -> a.getGroupId().equals("org.junit.platform"))
            .forEach(dependency -> classPath.add(dependency.getFile().getAbsolutePath()));
  }

  private Collection<Predicate<String>> globStringsToPredicates(
      final List<String> excludedMethods) {
    return asStream(excludedMethods)
            .map(Glob.toGlobPredicate())
            .collect(Collectors.toList());
  }

  private Collection<Predicate<String>> determineTargetTests() {
    return useConfiguredTargetTestsOrFindOccupiedPackages(this.mojo.getTargetTests()).stream()
            .map(Glob.toGlobPredicate())
            .collect(Collectors.toList());
  }

  private Collection<String> useConfiguredTargetTestsOrFindOccupiedPackages(
      final Collection<String> filters) {
    if (!hasValue(filters)) {
      this.mojo.getLog().info("Defaulting target tests to match packages in test build directory");
      return findOccupiedTestPackages();
    } else {
      return filters;
    }
  }

  private Collection<String> findOccupiedTestPackages() {
    // use only the tests within current project, even if in
    // cross module mode
    String outputDirName = this.mojo.getProject().getBuild()
        .getTestOutputDirectory();
    if (outputDirName != null) {
        File outputDir = new File(outputDirName);
        return findOccupiedPackagesIn(outputDir);
    } else {
        return Collections.emptyList();
    }
  }

  private Collection<Artifact> filteredDependencies() {
    return this.mojo.getPluginArtifactMap().values().stream()
            .filter(this.dependencyFilter)
            .collect(Collectors.toList());
  }

  private Collection<String> determineMutators() {
    if (this.mojo.getMutators() != null) {
      return this.mojo.getMutators();
    } else {
      return Collections.emptyList();
    }
  }

  private Collection<String> determineFeatures() {
      if (this.mojo.getFeatures() != null) {
        return this.mojo.getFeatures();
      } else {
        return Collections.emptyList();
      }
  }  
  
  private Collection<String> determineTargetClasses() {
    return useConfiguredTargetClassesOrFindOccupiedPackages(this.mojo.getTargetClasses());
  }

  private Collection<String> useConfiguredTargetClassesOrFindOccupiedPackages(
      final Collection<String> filters) {
    if (!hasValue(filters)) {
      this.mojo.getLog().info("Defaulting target classes to match packages in build directory");
      return findOccupiedPackages();
    } else {
      return filters;
    }
  }

  private Collection<String> findOccupiedPackages() {
    return Stream.concat(Stream.of(mojo.getProject()), dependedOnProjects().stream())
            .distinct()
            .map(p -> new File(p.getBuild().getOutputDirectory()))
            .flatMap(f -> findOccupiedPackagesIn(f).stream())
            .distinct()
            .collect(Collectors.toList());
  }
  
  public static Collection<String> findOccupiedPackagesIn(File dir) {
    if (dir.exists()) {
      DirectoryClassPathRoot root = new DirectoryClassPathRoot(dir);
      Set<String> occupiedPackages = new HashSet<>();
      FCollection.mapTo(root.classNames(), classToPackageGlob(),
          occupiedPackages);
      return occupiedPackages;
    }
    return Collections.emptyList();
  }
  
  private static Function<String,String> classToPackageGlob() {
    return a -> ClassName.fromString(a).getPackage().asJavaName() + ".*";
  }

  private Collection<Path> stringsToPaths(final List<String> sourceRoots) {
    return sourceRoots.stream()
            .map(Paths::get)
            .collect(Collectors.toList());
  }

  private Collection<String> determineOutputFormats() {
    if (hasValue(this.mojo.getOutputFormats())) {
      return this.mojo.getOutputFormats();
    } else {
      return Arrays.asList("HTML");
    }
  }

  private boolean hasValue(final Collection<?> collection) {
    return (collection != null) && !collection.isEmpty();
  }

  private Properties createPluginProperties() {
    Properties p = new Properties();
    if (this.mojo.getPluginProperties() != null) {
      p.putAll(this.mojo.getPluginProperties());
    }
    return p;
  }


  /**
   * This method taken from surefire under Apache 2 licence.
   *
   * Replaces expressions <pre>@{property-name}</pre> with the corresponding properties
   * from the model. This allows late evaluation of property values when the plugin is executed (as compared
   * to evaluation when the pom is parsed as is done with <pre>${property-name}</pre> expressions).
   *
   * This allows other plugins to modify or set properties with the changes getting picked up by surefire.
   */
  private String replacePropertyExpressions(String argLine) {
    for (Enumeration<?> e = mojo.getProject().getProperties().propertyNames(); e.hasMoreElements();) {

      String key = e.nextElement().toString();

      // Replace surefire late evaluation syntax properties
      argLine = replaceFieldForSymbol('@', key, argLine);

      // Normally properties will already have been expanded by maven, but this is
      // bypassed for argLines pulled from surefire, se we must handle them here
      argLine = replaceFieldForSymbol('$', key, argLine);
    }

    argLine = replaceSettingsField(argLine);

    return argLine;
  }

  private String replaceFieldForSymbol(char symbol, String key, String argLine) {
    String field = symbol + "{" + key + "}";
    if (argLine.contains(field))  {
      return argLine.replace(field, mojo.getProject().getProperties().getProperty(key, ""));
    }
    return argLine;
  }

  private String replaceSettingsField(String argLine) {
    String field = "${settings.localRepository}";
    if (argLine.contains(field))  {
      return argLine.replace(field, mojo.getSettings().getLocalRepository());
    }
    return argLine;
  }

}
