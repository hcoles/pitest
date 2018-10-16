/*
 * Copyright 2010 Henry Coles
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
package org.pitest.mutationtest.config;

import static org.pitest.functional.prelude.Prelude.not;
import static org.pitest.functional.prelude.Prelude.or;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.functional.FCollection;
import java.util.Optional;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.build.PercentAndConstantTimeoutStrategy;
import org.pitest.mutationtest.incremental.FileWriterFactory;
import org.pitest.mutationtest.incremental.NullWriterFactory;
import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.execute.Pitest;
import org.pitest.util.Glob;
import org.pitest.util.ResultOutputStrategy;
import org.pitest.util.Unchecked;

// FIXME move all logic to SettingsFactory and turn into simple bean

/**
 * Big ball of user supplied options to configure various aspects of mutation
 * testing.
 *
 */
public class ReportOptions {

  public static final Collection<String> LOGGING_CLASSES                = Arrays
      .asList(
          "java.util.logging",
          "org.apache.log4j",
          "org.apache.logging.log4j",
          "org.slf4j",
          "org.apache.commons.logging");

  private Collection<String>             targetClasses;
  private Collection<String>             excludedMethods                = Collections
      .emptyList();

  private Collection<String>             excludedClasses                = Collections
      .emptyList();

  private Collection<Predicate<String>>  excludedTestClasses            = Collections
      .emptyList();

  private Collection<String>             codePaths;

  private String                         reportDir;

  private File                           historyInputLocation;
  private File                           historyOutputLocation;

  private Collection<File>               sourceDirs;
  private Collection<String>             classPathElements;
  private Collection<String>             mutators;
  private Collection<String>             features;

  private int                            dependencyAnalysisMaxDistance;

  private final List<String>             jvmArgs                        = new ArrayList<>();
  private int                            numberOfThreads                = 0;
  private float                          timeoutFactor                  = PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR;
  private long                           timeoutConstant                = PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT;

  private Collection<Predicate<String>>  targetTests;

  private Collection<String>             loggingClasses                 = new ArrayList<>();

  private int                            maxMutationsPerClass;

  private boolean                        verbose                        = false;
  private boolean                        failWhenNoMutations            = false;
  private boolean                        skipFailingTests               = false;

  private final Collection<String>       outputs                        = new LinkedHashSet<>();

  private TestGroupConfig                groupConfig;

  private boolean                        fullMutationMatrix            = false;

  private int                            mutationUnitSize;
  private boolean                        shouldCreateTimestampedReports = true;
  private boolean                        detectInlinedCode              = false;
  private boolean                        exportLineCoverage             = false;
  private int                            mutationThreshold;
  private int                            coverageThreshold;

  private String                         mutationEngine                 = "gregor";

  private String                         javaExecutable;

  private boolean                        includeLaunchClasspath         = true;

  private Properties                     properties;

  private int maxSurvivors;

  private Collection<String>             excludedRunners                = new ArrayList<>();
  private Collection<String>             includedTestMethods            = new ArrayList<>();

  private String                         testPlugin                     = "";
  
  private boolean                        useClasspathJar;


  public boolean isVerbose() {
    return this.verbose;
  }

  /**
   * @return the reportDir
   */
  public String getReportDir() {
    return this.reportDir;
  }

  /**
   * @param reportDir
   *          the reportDir to set
   */
  public void setReportDir(final String reportDir) {
    this.reportDir = reportDir;
  }

  /**
   * @return the sourceDirs
   */
  public Collection<File> getSourceDirs() {
    return this.sourceDirs;
  }

  public Collection<String> getClassPathElements() {
    return this.classPathElements;
  }

  public void setClassPathElements(final Collection<String> classPathElements) {
    this.classPathElements = classPathElements;
  }

  /**
   * @param sourceDirs
   *          the sourceDirs to set
   */
  public void setSourceDirs(final Collection<File> sourceDirs) {
    this.sourceDirs = sourceDirs;
  }

  /**
   * @return the mutators
   */
  public Collection<String> getMutators() {
    return this.mutators;
  }

  /**
   * @param mutators
   *          the mutators to set
   */
  public void setMutators(final Collection<String> mutators) {
    this.mutators = mutators;
  }


  public Collection<String> getFeatures() {
    return this.features;
  }

  public void setFeatures(Collection<String> features) {
    this.features = features;
  }

  /**
   * @return the dependencyAnalysisMaxDistance
   */
  public int getDependencyAnalysisMaxDistance() {
    return this.dependencyAnalysisMaxDistance;
  }

  /**
   * @param dependencyAnalysisMaxDistance
   *          the dependencyAnalysisMaxDistance to set
   */
  public void setDependencyAnalysisMaxDistance(
      final int dependencyAnalysisMaxDistance) {
    this.dependencyAnalysisMaxDistance = dependencyAnalysisMaxDistance;
  }

  public List<String> getJvmArgs() {
    return this.jvmArgs;
  }

  public void addChildJVMArgs(final List<String> args) {
    this.jvmArgs.addAll(args);
  }

  public ClassPath getClassPath() {
    if (this.classPathElements != null) {
      return createClassPathFromElements();
    } else {
      return new ClassPath();
    }
  }

  private ClassPath createClassPathFromElements() {
    return new ClassPath(
        FCollection.map(this.classPathElements, stringToFile()));
  }

  private static Function<String, File> stringToFile() {
    return a -> new File(a);
  }

  public Collection<String> getTargetClasses() {
    return this.targetClasses;
  }


  public Predicate<String> getTargetClassesFilter() {
    final Predicate<String> filter = Prelude.and(or(Glob.toGlobPredicates(this.targetClasses)),
        not(isBlackListed(Glob.toGlobPredicates(ReportOptions.this.excludedClasses))));
    checkNotTryingToMutateSelf(filter);
    return filter;
  }

  private void checkNotTryingToMutateSelf(final Predicate<String> filter) {
    if (filter.test(Pitest.class.getName())) {
      throw new PitHelpError(Help.BAD_FILTER);
    }
  }

  public void setTargetClasses(final Collection<String> targetClasses) {
    this.targetClasses = targetClasses;
  }

  public void setTargetTests(
      final Collection<Predicate<String>> targetTestsPredicates) {
    this.targetTests = targetTestsPredicates;
  }

  public int getNumberOfThreads() {
    return this.numberOfThreads;
  }

  public void setNumberOfThreads(final int numberOfThreads) {
    this.numberOfThreads = numberOfThreads;
  }

  public float getTimeoutFactor() {
    return this.timeoutFactor;
  }

  public long getTimeoutConstant() {
    return this.timeoutConstant;
  }

  public void setTimeoutConstant(final long timeoutConstant) {
    this.timeoutConstant = timeoutConstant;
  }

  public void setTimeoutFactor(final float timeoutFactor) {
    this.timeoutFactor = timeoutFactor;
  }

  public Collection<Predicate<String>> getTargetTests() {
    return this.targetTests;
  }

  public Predicate<String> getTargetTestsFilter() {
    if ((this.targetTests == null) || this.targetTests.isEmpty()) {
      // If target tests is not explicitly set we assume that the
      // target classes predicate covers both classes and tests
      return Prelude.and(or(Glob.toGlobPredicates(this.targetClasses)),
          not(isBlackListed(ReportOptions.this.excludedTestClasses)));
    } else {
      return Prelude.and(or(this.targetTests),
          not(isBlackListed(ReportOptions.this.excludedTestClasses)));
    }
  }

  private static Predicate<String> isBlackListed(
      final Collection<Predicate<String>> excludedClasses) {
        return or(excludedClasses);
  }

  public Collection<String> getLoggingClasses() {
    if (this.loggingClasses.isEmpty()) {
      return LOGGING_CLASSES;
    } else {
      return this.loggingClasses;
    }
  }

  public void setLoggingClasses(final Collection<String> loggingClasses) {
    this.loggingClasses = loggingClasses;
  }

  public Collection<String> getExcludedMethods() {
    return this.excludedMethods;
  }

  public void setExcludedMethods(
      final Collection<String> excludedMethods) {
    this.excludedMethods = excludedMethods;
  }

  public void setVerbose(final boolean verbose) {
    this.verbose = verbose;
  }

  public void setExcludedClasses(
      final Collection<String> excludedClasses) {
    this.excludedClasses = excludedClasses;
  }

  public void setExcludedTestClasses(
      final Collection<Predicate<String>> excludedClasses) {
    this.excludedTestClasses = excludedClasses;
  }

  public void addOutputFormats(final Collection<String> formats) {
    this.outputs.addAll(formats);
  }

  public Collection<String> getOutputFormats() {
    return this.outputs;
  }

  public Collection<String> getExcludedClasses() {
    return this.excludedClasses;
  }

  public Collection<Predicate<String>> getExcludedTestClasses() {
    return this.excludedTestClasses;
  }

  public boolean shouldFailWhenNoMutations() {
    return this.failWhenNoMutations;
  }

  public void setFailWhenNoMutations(final boolean failWhenNoMutations) {
    this.failWhenNoMutations = failWhenNoMutations;
  }

  public boolean skipFailingTests() {
    return skipFailingTests;
  }

  public void setSkipFailingTests(final boolean skipFailingTests) {
    this.skipFailingTests = skipFailingTests;
  }

  public ProjectClassPaths getMutationClassPaths() {

    return new ProjectClassPaths(this.getClassPath(), createClassesFilter(),
        createPathFilter());
  }

  public ClassFilter createClassesFilter() {
    return new ClassFilter(this.getTargetTestsFilter(),
        this.getTargetClassesFilter());
  }

  private PathFilter createPathFilter() {
    return new PathFilter(createCodePathFilter(),
        not(new DefaultDependencyPathPredicate()));
  }

  private Predicate<ClassPathRoot> createCodePathFilter() {
    if ((this.codePaths != null) && !this.codePaths.isEmpty()) {
      return new PathNamePredicate(Prelude.or(Glob
          .toGlobPredicates(this.codePaths)));
    } else {
      return new DefaultCodePathPredicate();
    }
  }

  public Collection<String> getCodePaths() {
    return this.codePaths;
  }

  public void setCodePaths(final Collection<String> codePaths) {
    this.codePaths = codePaths;
  }

  public void setGroupConfig(final TestGroupConfig groupConfig) {
    this.groupConfig = groupConfig;
  }

  public TestGroupConfig getGroupConfig() {
    return this.groupConfig;
  }

  public void setFullMutationMatrix(final boolean fullMutationMatrix) {
    this.fullMutationMatrix = fullMutationMatrix;
  }

  public boolean isFullMutationMatrix() {
    return fullMutationMatrix;
  }

  public int getMutationUnitSize() {
    return this.mutationUnitSize;
  }

  public void setMutationUnitSize(final int size) {
    this.mutationUnitSize = size;
  }

  public ResultOutputStrategy getReportDirectoryStrategy() {
    return new DirectoryResultOutputStrategy(getReportDir(),
        pickDirectoryStrategy());
  }

  public void setShouldCreateTimestampedReports(
      final boolean shouldCreateTimestampedReports) {
    this.shouldCreateTimestampedReports = shouldCreateTimestampedReports;
  }

  private ReportDirCreationStrategy pickDirectoryStrategy() {
    if (this.shouldCreateTimestampedReports) {
      return new DatedDirectoryReportDirCreationStrategy();
    } else {
      return new UndatedReportDirCreationStrategy();
    }
  }

  public boolean shouldCreateTimeStampedReports() {
    return this.shouldCreateTimestampedReports;
  }

  public boolean isDetectInlinedCode() {
    return this.detectInlinedCode;
  }

  public void setDetectInlinedCode(final boolean b) {
    this.detectInlinedCode = b;
  }

  public WriterFactory createHistoryWriter() {
    if (this.historyOutputLocation == null) {
      return new NullWriterFactory();
    }

    return new FileWriterFactory(this.historyOutputLocation);
  }

  public Optional<Reader> createHistoryReader() {
    if (this.historyInputLocation == null) {
      return Optional.empty();
    }

    try {
      if (this.historyInputLocation.exists()
          && (this.historyInputLocation.length() > 0)) {
        return Optional.<Reader> ofNullable(new InputStreamReader(new FileInputStream(
            this.historyInputLocation), "UTF-8"));
      }
      return Optional.empty();
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  public void setHistoryInputLocation(final File historyInputLocation) {
    this.historyInputLocation = historyInputLocation;
  }

  public void setHistoryOutputLocation(final File historyOutputLocation) {
    this.historyOutputLocation = historyOutputLocation;
  }

  public File getHistoryInputLocation() {
    return this.historyInputLocation;
  }

  public File getHistoryOutputLocation() {
    return this.historyOutputLocation;
  }

  public void setExportLineCoverage(final boolean value) {
    this.exportLineCoverage = value;
  }

  public boolean shouldExportLineCoverage() {
    return this.exportLineCoverage;
  }

  public int getMutationThreshold() {
    return this.mutationThreshold;
  }

  public void setMutationThreshold(final int value) {
    this.mutationThreshold = value;
  }

  public String getMutationEngine() {
    return this.mutationEngine;
  }

  public void setMutationEngine(final String mutationEngine) {
    this.mutationEngine = mutationEngine;
  }

  public int getCoverageThreshold() {
    return this.coverageThreshold;
  }

  public void setCoverageThreshold(final int coverageThreshold) {
    this.coverageThreshold = coverageThreshold;
  }

  public String getJavaExecutable() {
    return this.javaExecutable;
  }

  public void setJavaExecutable(final String javaExecutable) {
    this.javaExecutable = javaExecutable;
  }

  public void setIncludeLaunchClasspath(final boolean b) {
    this.includeLaunchClasspath = b;
  }

  public boolean isIncludeLaunchClasspath() {
    return this.includeLaunchClasspath;
  }

  public Properties getFreeFormProperties() {
    return this.properties;
  }

  public void setFreeFormProperties(Properties props) {
    this.properties = props;
  }

  public int getMaximumAllowedSurvivors() {
    return this.maxSurvivors;
  }

  public void setMaximumAllowedSurvivors(int maxSurvivors) {
    this.maxSurvivors = maxSurvivors;
  }

  public Collection<String> getExcludedRunners() {
    return this.excludedRunners;
  }

  public Collection<String> getIncludedTestMethods() {
    return this.includedTestMethods;
  }

  public void setExcludedRunners(Collection<String> excludedRunners) {
    this.excludedRunners = excludedRunners;
  }

  public void setIncludedTestMethods(Collection<String> includedTestMethods) {
    this.includedTestMethods = includedTestMethods;
  }

  /**
   * Creates a serializable subset of data for use in child processes
   */
  public TestPluginArguments createMinionSettings() {
    return new TestPluginArguments(getTestPlugin(), this.getGroupConfig(), this.getExcludedRunners(),
            this.getIncludedTestMethods(), this.skipFailingTests());
  }

  public String getTestPlugin() {
    return this.testPlugin;
  }

  public void setTestPlugin(String testPlugin) {
    this.testPlugin = testPlugin;
  }

  public boolean useClasspathJar() {
    return useClasspathJar;
  }

  public void setUseClasspathJar(boolean useClasspathJar) {
    this.useClasspathJar = useClasspathJar;
  }

  @Override
  public String toString() {
    return "ReportOptions [targetClasses=" + targetClasses
        + ", excludedMethods=" + excludedMethods + ", excludedClasses="
        + excludedClasses + ", excludedTestClasses=" + excludedTestClasses
        + ", codePaths=" + codePaths + ", reportDir=" + reportDir
        + ", historyInputLocation=" + historyInputLocation
        + ", historyOutputLocation=" + historyOutputLocation + ", sourceDirs="
        + sourceDirs + ", classPathElements=" + classPathElements
        + ", mutators=" + mutators + ", features=" + features
        + ", dependencyAnalysisMaxDistance=" + dependencyAnalysisMaxDistance
        + ", jvmArgs=" + jvmArgs + ", numberOfThreads=" + numberOfThreads
        + ", timeoutFactor=" + timeoutFactor + ", timeoutConstant="
        + timeoutConstant + ", targetTests=" + targetTests + ", loggingClasses="
        + loggingClasses + ", maxMutationsPerClass=" + maxMutationsPerClass
        + ", verbose=" + verbose + ", failWhenNoMutations="
        + failWhenNoMutations + ", outputs=" + outputs + ", groupConfig="
        + groupConfig + ", fullMutationMatrix=" + fullMutationMatrix + ", mutationUnitSize=" + mutationUnitSize
        + ", shouldCreateTimestampedReports=" + shouldCreateTimestampedReports
        + ", detectInlinedCode=" + detectInlinedCode + ", exportLineCoverage="
        + exportLineCoverage + ", mutationThreshold=" + mutationThreshold
        + ", coverageThreshold=" + coverageThreshold + ", mutationEngine="
        + mutationEngine + ", javaExecutable=" + javaExecutable
        + ", includeLaunchClasspath=" + includeLaunchClasspath + ", properties="
        + properties + ", maxSurvivors=" + maxSurvivors + ", excludedRunners="
        + excludedRunners + ", includedTestMethods=" + includedTestMethods
        + ", testPlugin=" + testPlugin + ", useClasspathJar=" + useClasspathJar
        + ", skipFailingTests=" + skipFailingTests + "]";
  }

  


}
