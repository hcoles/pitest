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
import org.apache.maven.model.Plugin;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class MojoToReportOptionsConverter {

  private PitMojo                 mojo;
  private Predicate<Artifact>     dependencyFilter;
  private Log                     log;
  private SurefireConfigConverter surefireConverter;

  public MojoToReportOptionsConverter( PitMojo mojo,
                                       SurefireConfigConverter surefireConverter,
                                       Predicate<Artifact> dependencyFilter) {
    this.mojo = mojo;
    this.dependencyFilter = dependencyFilter;
    this.surefireConverter = surefireConverter;

    this.log = mojo.getLog();
  }

  @SuppressWarnings("unchecked")
  public ReportOptions convert() {

    List<String> classPath = new ArrayList<String>();

    try {
      classPath.addAll(mojo.getProject().getTestClasspathElements());
    } catch (DependencyResolutionRequiredException e) {
      log.info(e);
    }

    addOwnDependenciesToClassPath(classPath);

    classPath.addAll(mojo.getAdditionalClasspathElements());

    for (Object artifact : mojo.getProject().getArtifacts()) {
      Artifact dependency = (Artifact) artifact;

      String identifier = dependency.getGroupId() + ":" + dependency.getArtifactId();
      if (mojo.getClasspathDependencyExcludes().contains(identifier)) {
        classPath.remove(dependency.getFile().getPath());
      }
    }

    return updateFromSurefire(parseReportOptions(classPath));

  }

  @SuppressWarnings("unchecked")
  private ReportOptions parseReportOptions(List<String> classPath) {
    ReportOptions data = new ReportOptions();

    if (mojo.getProject().getBuild() != null) {
      String outputDirectory = mojo.getProject().getBuild().getOutputDirectory();
      log.info("Mutating from " + outputDirectory);
      data.setCodePaths(Collections.singleton(outputDirectory));
    }

    data.setClassPathElements(classPath);
    data.setDependencyAnalysisMaxDistance(mojo.getMaxDependencyDistance());
    data.setFailWhenNoMutations(shouldFailWhenNoMutations());

    data.setTargetClasses(determineTargetClasses());
    data.setTargetTests(determineTargetTests());

    data.setMutateStaticInitializers(mojo.isMutateStaticInitializers());
    data.setExcludedMethods(globStringsToPredicates(mojo
        .getExcludedMethods()));
    data.setExcludedClasses(globStringsToPredicates(mojo
        .getExcludedClasses()));
    data.setNumberOfThreads(mojo.getThreads());
    data.setMaxMutationsPerClass(mojo.getMaxMutationsPerClass());

    data.setReportDir(mojo.getReportsDirectory().getAbsolutePath());
    data.setVerbose(mojo.isVerbose());
    if (mojo.getJvmArgs() != null) {
      data.addChildJVMArgs(mojo.getJvmArgs());
    }

    data.setMutators(determineMutators());
    data.setTimeoutConstant(mojo.getTimeoutConstant());
    data.setTimeoutFactor(mojo.getTimeoutFactor());
    if (hasValue(mojo.getAvoidCallsTo())) {
      data.setLoggingClasses(mojo.getAvoidCallsTo());
    }

    List<String> sourceRoots = new ArrayList<String>();
    sourceRoots.addAll(mojo.getProject().getCompileSourceRoots());
    sourceRoots.addAll(mojo.getProject().getTestCompileSourceRoots());

    data.setSourceDirs(stringsTofiles(sourceRoots));

    data.addOutputFormats(determineOutputFormats());

    setTestGroups(data);

    data.setMutationUnitSize(mojo.getMutationUnitSize());
    data.setShouldCreateTimestampedReports(mojo.isTimestampedReports());
    data.setDetectInlinedCode(mojo.isDetectInlinedCode());

    data.setHistoryInputLocation(mojo.getHistoryInputFile());
    data.setHistoryOutputLocation(mojo.getHistoryOutputFile());
    data.setExportLineCoverage(mojo.isExportLineCoverage());
    data.setMutationEngine(mojo.getMutationEngine());
    data.setJavaExecutable(mojo.getJavaExecutable());
    data.setFreeFormProperties(createPluginProperties());

    return data;
  }

  private ReportOptions updateFromSurefire(ReportOptions option) {
    Collection<Plugin> plugins = lookupPlugin("org.apache.maven.plugins:maven-surefire-plugin");
    if (!mojo.isParseSurefireConfig()) {
      return option;
    } else if (plugins.isEmpty()) {
      log.warn("Could not find surefire configuration in pom");
      return option;
    }

    Plugin surefire = plugins.iterator().next();
    if (surefire != null) {
      return surefireConverter.update(option, (Xpp3Dom) surefire.getConfiguration());
    } else {
      return option;
    }
  }

  private Collection<Plugin> lookupPlugin(String key) {
    @SuppressWarnings("unchecked")
    List<Plugin> plugins = mojo.getProject().getBuildPlugins();
    return FCollection.filter(plugins, hasKey(key));
  }

  private static F<Plugin, Boolean> hasKey(final String key) {
    return new F<Plugin, Boolean>() {
      public Boolean apply(Plugin a) {
        return a.getKey().equals(key);
      }
    };
  }

  private boolean shouldFailWhenNoMutations() {
    return mojo.isFailWhenNoMutations();
  }

  private void setTestGroups(ReportOptions data) {
    TestGroupConfig conf = new TestGroupConfig(mojo.getExcludedGroups(), mojo.getIncludedGroups());
    data.setGroupConfig(conf);
  }

  private void addOwnDependenciesToClassPath(List<String> classPath) {
    for (Artifact dependency : filteredDependencies()) {
      log.info("Adding " + dependency.getGroupId() + ":" + dependency.getArtifactId() + " to SUT classpath");
      classPath.add(dependency.getFile().getAbsolutePath());
    }
  }

  private Collection<Predicate<String>> globStringsToPredicates(List<String> excludedMethods) {
    return FCollection.map(excludedMethods, Glob.toGlobPredicate());
  }

  private Collection<Predicate<String>> determineTargetTests() {
    return FCollection.map(mojo.getTargetTests(), Glob.toGlobPredicate());
  }

  private Collection<Artifact> filteredDependencies() {
    return FCollection.filter(mojo.getPluginArtifactMap().values(),dependencyFilter);
  }

  private Collection<String> determineMutators() {
    if (mojo.getMutators() != null) {
      return mojo.getMutators();
    } else {
      return Collections.emptyList();
    }
  }

  private Collection<Predicate<String>> determineTargetClasses() {
    return returnOrDefaultToClassesLikeGroupName(mojo.getTargetClasses());
  }

  private Collection<Predicate<String>> returnOrDefaultToClassesLikeGroupName(
      Collection<String> filters) {
    if (!hasValue(filters)) {
      String groupId = mojo.getProject().getGroupId() + "*";
      mojo.getLog().info("Defaulting to group id (" + groupId + ")");
      return Collections.<Predicate<String>> singleton(new Glob(groupId));
    } else {
      return FCollection.map(filters, Glob.toGlobPredicate());
    }
  }

  private Collection<File> stringsTofiles(List<String> sourceRoots) {
    return FCollection.map(sourceRoots, stringToFile());
  }

  private F<String, File> stringToFile() {
    return new F<String, File>() {
      public File apply(String a) {
        return new File(a);
      }

    };
  }

  private Collection<String> determineOutputFormats() {
    if (hasValue(mojo.getOutputFormats())) {
      return mojo.getOutputFormats();
    } else {
      return Arrays.asList("HTML");
    }
  }

  private boolean hasValue(Collection<?> collection) {
    return (collection != null) && !collection.isEmpty();
  }

  private Properties createPluginProperties() {
    Properties p = new Properties();
    if (mojo.getPluginProperties() != null) {
      p.putAll(mojo.getPluginProperties());
    }
    return p;
  }


}
