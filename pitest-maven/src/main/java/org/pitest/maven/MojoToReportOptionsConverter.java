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

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.pitest.classinfo.ClassName;
import org.pitest.classpath.DirectoryClassPathRoot;
import java.util.function.Function;
import org.pitest.functional.FCollection;
import java.util.function.Predicate;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;

public class MojoToReportOptionsConverter {

  private final AbstractPitMojo                 mojo;
  private final Predicate<Artifact>     dependencyFilter;
  private final Log                     log;
  private final SurefireConfigConverter surefireConverter;

  public MojoToReportOptionsConverter(final AbstractPitMojo mojo,
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

    for (Object artifact : this.mojo.getProject().getArtifacts()) {
      final Artifact dependency = (Artifact) artifact;

      if (this.mojo.getClasspathDependencyExcludes().contains(
          dependency.getGroupId() + ":" + dependency.getArtifactId())) {
        classPath.remove(dependency.getFile().getPath());
      }
    }

    ReportOptions option = parseReportOptions(classPath);
    return updateFromSurefire(option);

  }

  private ReportOptions parseReportOptions(final List<String> classPath) {
    final ReportOptions data = new ReportOptions();

    if (this.mojo.getProject().getBuild() != null) {
      this.log.info("Mutating from "
          + this.mojo.getProject().getBuild().getOutputDirectory());
      data.setCodePaths(Collections.singleton(this.mojo.getProject().getBuild()
          .getOutputDirectory()));
    }

    data.setTestPlugin(this.mojo.getTestPlugin());
    data.setClassPathElements(classPath);
    data.setDependencyAnalysisMaxDistance(this.mojo.getMaxDependencyDistance());
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
    data.setVerbose(this.mojo.isVerbose());
    if (this.mojo.getJvmArgs() != null) {
      data.addChildJVMArgs(this.mojo.getJvmArgs());
    }

    data.setMutators(determineMutators());
    data.setFeatures(determineFeatures());
    data.setTimeoutConstant(this.mojo.getTimeoutConstant());
    data.setTimeoutFactor(this.mojo.getTimeoutFactor());
    if (hasValue(this.mojo.getAvoidCallsTo())) {
      data.setLoggingClasses(this.mojo.getAvoidCallsTo());
    }

    final List<String> sourceRoots = new ArrayList<>();
    sourceRoots.addAll(this.mojo.getProject().getCompileSourceRoots());
    sourceRoots.addAll(this.mojo.getProject().getTestCompileSourceRoots());

    data.setSourceDirs(stringsTofiles(sourceRoots));

    data.addOutputFormats(determineOutputFormats());

    setTestGroups(data);

    data.setMutationUnitSize(this.mojo.getMutationUnitSize());
    data.setShouldCreateTimestampedReports(this.mojo.isTimestampedReports());
    data.setDetectInlinedCode(this.mojo.isDetectInlinedCode());

    determineHistory(data);
    
    data.setExportLineCoverage(this.mojo.isExportLineCoverage());
    data.setMutationEngine(this.mojo.getMutationEngine());
    data.setJavaExecutable(this.mojo.getJavaExecutable());
    data.setFreeFormProperties(createPluginProperties());
    data.setIncludedTestMethods(this.mojo.getIncludedTestMethods());

    return data;
  }


  private void determineHistory(final ReportOptions data) {
    if (this.mojo.useHistory()) {
      useHistoryFileInTempDir(data);
    } else {
      data.setHistoryInputLocation(this.mojo.getHistoryInputFile());
      data.setHistoryOutputLocation(this.mojo.getHistoryOutputFile());
    }
  }

  private void useHistoryFileInTempDir(final ReportOptions data) {
    String tempDir = System.getProperty("java.io.tmpdir");
    MavenProject project = this.mojo.project;
    String name = project.getGroupId() + "."
        + project.getArtifactId() + "."
        + project.getVersion() + "_pitest_history.bin";
    File historyFile = new File(tempDir, name);
    log.info("Will read and write history at " + historyFile);
    if (this.mojo.getHistoryInputFile() == null) {
      data.setHistoryInputLocation(historyFile);
    }
    if (this.mojo.getHistoryOutputFile() == null) {
      data.setHistoryOutputLocation(historyFile);
    }
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
    return new Predicate<Plugin>() {
      @Override
      public boolean test(Plugin a) {
        return a.getKey().equals(key);
      }
    };
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
  }

  private Collection<Predicate<String>> globStringsToPredicates(
      final List<String> excludedMethods) {
    return FCollection.map(excludedMethods, Glob.toGlobPredicate());
  }

  private Collection<Predicate<String>> determineTargetTests() {
    return FCollection.map(this.mojo.getTargetTests(), Glob.toGlobPredicate());
  }

  private Collection<Artifact> filteredDependencies() {
    return FCollection.filter(this.mojo.getPluginArtifactMap().values(),
        this.dependencyFilter);
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
    String outputDirName = this.mojo.getProject().getBuild()
        .getOutputDirectory();
    File outputDir = new File(outputDirName);
    if (outputDir.exists()) {
      DirectoryClassPathRoot root = new DirectoryClassPathRoot(outputDir);
      Set<String> occupiedPackages = new HashSet<>();
      FCollection.mapTo(root.classNames(), classToPackageGlob(),
          occupiedPackages);
      return occupiedPackages;
    }
    return Collections.emptyList();
  }
  
  private static Function<String,String> classToPackageGlob() {
    return new Function<String,String>() {
      @Override
      public String apply(String a) {
        return ClassName.fromString(a).getPackage().asJavaName() + ".*";
      }
    };
  }

  private Collection<File> stringsTofiles(final List<String> sourceRoots) {
    return FCollection.map(sourceRoots, stringToFile());
  }

  private Function<String, File> stringToFile() {
    return new Function<String, File>() {
      @Override
      public File apply(final String a) {
        return new File(a);
      }
    };
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

}
