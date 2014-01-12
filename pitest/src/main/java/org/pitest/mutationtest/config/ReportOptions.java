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

import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.execute.Pitest;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.help.Help;
import org.pitest.help.PitHelpError;
import org.pitest.mutationtest.build.PercentAndConstantTimeoutStrategy;
import org.pitest.mutationtest.incremental.FileWriterFactory;
import org.pitest.mutationtest.incremental.NullWriterFactory;
import org.pitest.mutationtest.incremental.WriterFactory;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.TestGroupConfig;
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

  public final static Collection<String> LOGGING_CLASSES                = Arrays
                                                                            .asList(
                                                                                "java.util.logging",
                                                                                "org.apache.log4j",
                                                                                "org.slf4j",
                                                                                "org.apache.commons.logging");

  private Configuration                  config;
  private Collection<Predicate<String>>  targetClasses;
  private Collection<Predicate<String>>  excludedMethods                = Collections
                                                                            .emptyList();

  private Collection<Predicate<String>>  excludedClasses                = Collections
                                                                            .emptyList();

  private Collection<String>             codePaths;

  private String                         reportDir;

  private File                           historyInputLocation;
  private File                           historyOutputLocation;

  private Collection<File>               sourceDirs;
  private Collection<String>             classPathElements;
  private Collection<String>             mutators;

  private int                            dependencyAnalysisMaxDistance;
  private boolean                        mutateStaticInitializers       = false;

  private final List<String>             jvmArgs                        = new ArrayList<String>();
  private int                            numberOfThreads                = 0;
  private float                          timeoutFactor                  = PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR;
  private long                           timeoutConstant                = PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT;

  private Collection<Predicate<String>>  targetTests;

  private Collection<String>             loggingClasses                 = new ArrayList<String>();

  private int                            maxMutationsPerClass;

  private boolean                        verbose                        = false;
  private boolean                        failWhenNoMutations            = false;

  private final Collection<String>       outputs                        = new LinkedHashSet<String>();

  private TestGroupConfig groupConfig;

  private int                            mutationUnitSize;
  private boolean                        shouldCreateTimestampedReports = true;
  private boolean                        detectInlinedCode              = false;
  private boolean                        exportLineCoverage             = false;
  private int                            mutationThreshold;
  private int                            coverageThreshold;

  private String                         mutationEngine                 = "gregor";

  private String                         javaExecutable;

  private boolean                        includeLaunchClasspath         = true;

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

  private static F<String, File> stringToFile() {
    return new F<String, File>() {

      public File apply(final String a) {
        return new File(a);
      }

    };
  }

  public Collection<Predicate<String>> getTargetClasses() {
    return this.targetClasses;
  }

  @SuppressWarnings("unchecked")
  public Predicate<String> getTargetClassesFilter() {
    final Predicate<String> filter = Prelude.and(or(this.targetClasses),
        not(isBlackListed(ReportOptions.this.excludedClasses)));
    checkNotTryingToMutateSelf(filter);
    return filter;
  }

  private void checkNotTryingToMutateSelf(final Predicate<String> filter) {
    if (filter.apply(Pitest.class.getName())) {
      throw new PitHelpError(Help.BAD_FILTER);
    }
  }

  public void setTargetClasses(final Collection<Predicate<String>> targetClasses) {
    this.targetClasses = targetClasses;
  }

  public void setTargetTests(
      final Collection<Predicate<String>> targetTestsPredicates) {
    this.targetTests = targetTestsPredicates;
  }

  public boolean isMutateStaticInitializers() {
    return this.mutateStaticInitializers;
  }

  public void setMutateStaticInitializers(final boolean mutateStaticInitializers) {
    this.mutateStaticInitializers = mutateStaticInitializers;
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

  @SuppressWarnings("unchecked")
  public Predicate<String> getTargetTestsFilter() {
    if ((this.targetTests == null) || this.targetTests.isEmpty()) {
      return this.getTargetClassesFilter(); // if no tests specified assume the
                                            // target classes filter covers both
    } else {
      return Prelude.and(or(this.targetTests),
          not(isBlackListed(ReportOptions.this.excludedClasses)));
    }

  }

  private static Predicate<String> isBlackListed(
      final Collection<Predicate<String>> excludedClasses) {
    return new Predicate<String>() {

      public Boolean apply(final String a) {
        return or(excludedClasses).apply(a);
      }

    };
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

  public Collection<Predicate<String>> getExcludedMethods() {
    return this.excludedMethods;
  }

  public void setExcludedMethods(
      final Collection<Predicate<String>> excludedMethods) {
    this.excludedMethods = excludedMethods;
  }

  public int getMaxMutationsPerClass() {
    return this.maxMutationsPerClass;
  }

  public void setMaxMutationsPerClass(final int maxMutationsPerClass) {
    this.maxMutationsPerClass = maxMutationsPerClass;
  }

  public void setVerbose(final boolean verbose) {
    this.verbose = verbose;
  }

  public void setExcludedClasses(
      final Collection<Predicate<String>> excludedClasses) {
    this.excludedClasses = excludedClasses;
  }

  public void addOutputFormats(final Collection<String> formats) {
    this.outputs.addAll(formats);
  }

  public Collection<String> getOutputFormats() {
    return this.outputs;
  }

  public Collection<Predicate<String>> getExcludedClasses() {
    return this.excludedClasses;
  }

  public boolean shouldFailWhenNoMutations() {
    return this.failWhenNoMutations;
  }

  public void setFailWhenNoMutations(final boolean failWhenNoMutations) {
    this.failWhenNoMutations = failWhenNoMutations;
  }

  @SuppressWarnings("unchecked")
  public CoverageOptions createCoverageOptions() {
    return new CoverageOptions(Prelude.and(this.getTargetClassesFilter(),
        not(commonClasses())), this.config, this.isVerbose(),
        this.getDependencyAnalysisMaxDistance());
  }

  private static F<String, Boolean> commonClasses() {
    return new F<String, Boolean>() {
      public Boolean apply(final String name) {
        return name.startsWith("java") || name.startsWith("sun/")
            || name.startsWith("org/junit") || name.startsWith("junit")
            || name.startsWith("org/pitest/coverage")
            || name.startsWith("org/pitest/reloc")
            || name.startsWith("org/pitest/boot");
      }

    };
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

  public void setConfiguration(final Configuration configuration) {
    this.config = configuration;
  }

  public void setGroupConfig(final TestGroupConfig groupConfig) {
    this.groupConfig = groupConfig;
  }

  public TestGroupConfig getGroupConfig() {
    return this.groupConfig;
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

  public Option<Reader> createHistoryReader() {
    if (this.historyInputLocation == null) {
      return Option.none();
    }

    try {
      if (this.historyInputLocation.exists()
          && (this.historyInputLocation.length() > 0)) {
        return Option.<Reader> some(new InputStreamReader(new FileInputStream(
            this.historyInputLocation), "UTF-8"));
      }
      return Option.none();
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

  @Override
  public String toString() {
    return "ReportOptions [config=" + this.config + ", targetClasses="
        + this.targetClasses + ", excludedMethods=" + this.excludedMethods
        + ", excludedClasses=" + this.excludedClasses + ", codePaths="
        + this.codePaths + ", reportDir=" + this.reportDir
        + ", historyInputLocation=" + this.historyInputLocation
        + ", historyOutputLocation=" + this.historyOutputLocation
        + ", sourceDirs=" + this.sourceDirs + ", classPathElements="
        + this.classPathElements + ", mutators=" + this.mutators
        + ", dependencyAnalysisMaxDistance="
        + this.dependencyAnalysisMaxDistance + ", mutateStaticInitializers="
        + this.mutateStaticInitializers + ", jvmArgs=" + this.jvmArgs
        + ", numberOfThreads=" + this.numberOfThreads + ", timeoutFactor="
        + this.timeoutFactor + ", timeoutConstant=" + this.timeoutConstant
        + ", targetTests=" + this.targetTests + ", loggingClasses="
        + this.loggingClasses + ", maxMutationsPerClass="
        + this.maxMutationsPerClass + ", verbose=" + this.verbose
        + ", failWhenNoMutations=" + this.failWhenNoMutations + ", outputs="
        + this.outputs + ", groupConfig=" + this.groupConfig
        + ", mutationUnitSize=" + this.mutationUnitSize
        + ", shouldCreateTimestampedReports="
        + this.shouldCreateTimestampedReports + ", detectInlinedCode="
        + this.detectInlinedCode + ", exportLineCoverage="
        + this.exportLineCoverage + ", mutationThreshold="
        + this.mutationThreshold + ", coverageThreshold="
        + this.coverageThreshold + ", mutationEngine=" + this.mutationEngine
        + ", javaExecutable=" + this.javaExecutable + "]";
  }

}
