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
package org.pitest.mutationtest.commandline;

import static org.pitest.mutationtest.config.ConfigOption.AVOID_CALLS;
import static org.pitest.mutationtest.config.ConfigOption.CHILD_JVM;
import static org.pitest.mutationtest.config.ConfigOption.CLASSPATH;
import static org.pitest.mutationtest.config.ConfigOption.CODE_PATHS;
import static org.pitest.mutationtest.config.ConfigOption.COVERAGE_THRESHOLD;
import static org.pitest.mutationtest.config.ConfigOption.DEPENDENCY_DISTANCE;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_GROUPS;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_METHOD;
import static org.pitest.mutationtest.config.ConfigOption.EXPORT_LINE_COVERAGE;
import static org.pitest.mutationtest.config.ConfigOption.FAIL_WHEN_NOT_MUTATIONS;
import static org.pitest.mutationtest.config.ConfigOption.HISTORY_INPUT_LOCATION;
import static org.pitest.mutationtest.config.ConfigOption.HISTORY_OUTPUT_LOCATION;
import static org.pitest.mutationtest.config.ConfigOption.INCLUDED_GROUPS;
import static org.pitest.mutationtest.config.ConfigOption.INCLUDE_LAUNCH_CLASSPATH;
import static org.pitest.mutationtest.config.ConfigOption.JVM_PATH;
import static org.pitest.mutationtest.config.ConfigOption.MAX_MUTATIONS_PER_CLASS;
import static org.pitest.mutationtest.config.ConfigOption.MUTATE_STATIC_INITIALIZERS;
import static org.pitest.mutationtest.config.ConfigOption.MUTATIONS;
import static org.pitest.mutationtest.config.ConfigOption.MUTATION_ENGINE;
import static org.pitest.mutationtest.config.ConfigOption.MUTATION_THRESHOLD;
import static org.pitest.mutationtest.config.ConfigOption.MUTATION_UNIT_SIZE;
import static org.pitest.mutationtest.config.ConfigOption.OUTPUT_FORMATS;
import static org.pitest.mutationtest.config.ConfigOption.PROJECT_FILE;
import static org.pitest.mutationtest.config.ConfigOption.REPORT_DIR;
import static org.pitest.mutationtest.config.ConfigOption.SOURCE_DIR;
import static org.pitest.mutationtest.config.ConfigOption.TARGET_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.TEST_FILTER;
import static org.pitest.mutationtest.config.ConfigOption.THREADS;
import static org.pitest.mutationtest.config.ConfigOption.TIMEOUT_CONST;
import static org.pitest.mutationtest.config.ConfigOption.TIMEOUT_FACTOR;
import static org.pitest.mutationtest.config.ConfigOption.TIME_STAMPED_REPORTS;
import static org.pitest.mutationtest.config.ConfigOption.USE_INLINED_CODE_DETECTION;
import static org.pitest.mutationtest.config.ConfigOption.VERBOSE;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

import org.pitest.classpath.ClassPath;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.config.ConfigOption;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectConfigurationParser;
import org.pitest.project.ProjectConfigurationParserException;
import org.pitest.project.ProjectConfigurationParserFactory;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;
import org.pitest.util.Unchecked;

public class OptionsParser {

  private final Predicate<String>                    dependencyFilter;

  private final OptionParser                         parser;
  private final ArgumentAcceptingOptionSpec<String>  reportDirSpec;
  private final OptionSpec<String>                   targetClassesSpec;
  private final OptionSpec<String>                   targetTestsSpec;
  private final OptionSpec<String>                   avoidCallsSpec;
  private final OptionSpec<Integer>                  depth;
  private final OptionSpec<Integer>                  threadsSpec;
  private final OptionSpec<File>                     sourceDirSpec;
  private final OptionSpec<File>                     historyOutputSpec;
  private final OptionSpec<File>                     historyInputSpec;
  private final OptionSpec<String>                   mutators;
  private final OptionSpec<String>                   jvmArgs;
  private final ArgumentAcceptingOptionSpec<Boolean> mutateStatics;
  private final OptionSpec<Float>                    timeoutFactorSpec;
  private final OptionSpec<Long>                     timeoutConstSpec;
  private final OptionSpec<String>                   excludedMethodsSpec;
  private final OptionSpec<Integer>                  maxMutationsPerClassSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> verboseSpec;
  private final OptionSpec<String>                   excludedClassesSpec;
  private final OptionSpec<String>                   outputFormatSpec;
  private final OptionSpec<String>                   projectFileSpec;
  private final OptionSpec<String>                   additionalClassPathSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> failWhenNoMutations;
  private final ArgumentAcceptingOptionSpec<String>  codePaths;
  private final OptionSpec<String>                   excludedGroupsSpec;
  private final OptionSpec<String>                   includedGroupsSpec;
  private final OptionSpec<Integer>                  mutationUnitSizeSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> timestampedReportsSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> detectInlinedCode;
  private final ArgumentAcceptingOptionSpec<Integer> mutationThreshHoldSpec;
  private final ArgumentAcceptingOptionSpec<Integer> coverageThreshHoldSpec;
  private final OptionSpec<String>                   mutationEngine;
  private final ArgumentAcceptingOptionSpec<Boolean> exportLineCoverageSpec;
  private final OptionSpec<String>                   javaExecutable;

  private final ArgumentAcceptingOptionSpec<Boolean> includeLaunchClasspathSpec;

  public OptionsParser(Predicate<String> dependencyFilter) {

    this.dependencyFilter = dependencyFilter;

    this.parser = new OptionParser();
    this.parser.acceptsAll(Arrays.asList("h", "?"), "show help");

    this.reportDirSpec = parserAccepts(REPORT_DIR).withRequiredArg()
        .describedAs("directory to create report folder in").required();

    this.projectFileSpec = parserAccepts(PROJECT_FILE).withRequiredArg()
        .ofType(String.class)
        .describedAs("The name of the config file to use.");

    this.targetClassesSpec = parserAccepts(TARGET_CLASSES)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma separated list of filters to match against classes to test")
        .required();

    this.avoidCallsSpec = parserAccepts(AVOID_CALLS)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma separated list of packages to consider as untouchable logging calls");

    this.targetTestsSpec = parserAccepts(TEST_FILTER)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma separated list of filters to match against tests to run");

    this.depth = parserAccepts(DEPENDENCY_DISTANCE).withRequiredArg()
        .ofType(Integer.class)
        .defaultsTo(DEPENDENCY_DISTANCE.getDefault(Integer.class))
        .describedAs("maximum distance to look from test for covered classes");

    this.threadsSpec = parserAccepts(THREADS).withRequiredArg()
        .ofType(Integer.class).defaultsTo(THREADS.getDefault(Integer.class))
        .describedAs("number of threads to use for testing");

    this.maxMutationsPerClassSpec = parserAccepts(MAX_MUTATIONS_PER_CLASS)
        .withRequiredArg().ofType(Integer.class)
        .defaultsTo(MAX_MUTATIONS_PER_CLASS.getDefault(Integer.class))
        .describedAs("max number of mutations to allow for each class");

    this.sourceDirSpec = parserAccepts(SOURCE_DIR).withRequiredArg()
        .ofType(File.class).withValuesSeparatedBy(',')
        .describedAs("comma separated list of source directories").required();

    this.mutators = parserAccepts(MUTATIONS).withRequiredArg()
        .ofType(String.class).withValuesSeparatedBy(',')
        .describedAs("comma separated list of mutation operators");

    this.jvmArgs = parserAccepts(CHILD_JVM).withRequiredArg()
        .withValuesSeparatedBy(',')
        .describedAs("comma separated list of child JVM args");

    this.mutateStatics = parserAccepts(MUTATE_STATIC_INITIALIZERS)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(true)
        .describedAs(
            "whether or not to generate mutations in static initializers");

    this.detectInlinedCode = parserAccepts(USE_INLINED_CODE_DETECTION)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(true)
        .describedAs(
            "whether or not to try and detect code inlined from finally blocks");

    this.timestampedReportsSpec = parserAccepts(TIME_STAMPED_REPORTS)
        .withOptionalArg().ofType(Boolean.class).defaultsTo(true)
        .describedAs("whether or not to generated timestamped directories");

    this.timeoutFactorSpec = parserAccepts(TIMEOUT_FACTOR).withOptionalArg()
        .ofType(Float.class)
        .describedAs("factor to apply to calculate maximum test duration")
        .defaultsTo(TIMEOUT_FACTOR.getDefault(Float.class));

    this.timeoutConstSpec = parserAccepts(TIMEOUT_CONST).withOptionalArg()
        .ofType(Long.class)
        .describedAs("constant to apply to calculate maximum test duration")
        .defaultsTo(TIMEOUT_CONST.getDefault(Long.class));

    this.excludedMethodsSpec = parserAccepts(EXCLUDED_METHOD)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma separated list of filters to match against methods to exclude from mutation analysis");

    this.excludedClassesSpec = parserAccepts(EXCLUDED_CLASSES)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma separated list of globs for classes to exclude when looking for both mutation target and tests");

    this.verboseSpec = parserAccepts(VERBOSE).withOptionalArg()
        .ofType(Boolean.class).defaultsTo(true)
        .describedAs("whether or not to generate verbose output");

    this.exportLineCoverageSpec = parserAccepts(EXPORT_LINE_COVERAGE)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(true)
        .describedAs(
            "whether or not to dump per test line coverage data to disk");

    this.includeLaunchClasspathSpec = parserAccepts(INCLUDE_LAUNCH_CLASSPATH)
        .withOptionalArg().ofType(Boolean.class).defaultsTo(true)
        .describedAs("whether or not to analyse launch classpath");

    this.outputFormatSpec = parserAccepts(OUTPUT_FORMATS)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma separated list of listeners to receive mutation results")
        .defaultsTo("HTML");

    this.additionalClassPathSpec = parserAccepts(CLASSPATH).withRequiredArg()
        .ofType(String.class).withValuesSeparatedBy(',')
        .describedAs("coma separated list of additional classpath elements");

    this.failWhenNoMutations = parserAccepts(FAIL_WHEN_NOT_MUTATIONS)
        .withOptionalArg().ofType(Boolean.class).defaultsTo(true)
        .describedAs("whether to throw error if no mutations found");

    this.codePaths = parserAccepts(CODE_PATHS)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "Globs identifying classpath roots containing mutable code");

    this.includedGroupsSpec = parserAccepts(INCLUDED_GROUPS).withRequiredArg()
        .ofType(String.class).withValuesSeparatedBy(',')
        .describedAs("TestNG groups/JUnit categories to include");

    this.excludedGroupsSpec = parserAccepts(EXCLUDED_GROUPS).withRequiredArg()
        .ofType(String.class).withValuesSeparatedBy(',')
        .describedAs("TestNG groups/JUnit categories to include");

    this.mutationUnitSizeSpec = parserAccepts(MUTATION_UNIT_SIZE)
        .withRequiredArg()
        .ofType(Integer.class)
        .describedAs(
            "Maximum number of mutations to include within a single unit of analysis")
        .defaultsTo(MUTATION_UNIT_SIZE.getDefault(Integer.class));

    this.historyInputSpec = parserAccepts(HISTORY_INPUT_LOCATION)
        .withRequiredArg().ofType(File.class)
        .describedAs("File to read history from for incremental analysis");

    this.historyOutputSpec = parserAccepts(HISTORY_OUTPUT_LOCATION)
        .withRequiredArg().ofType(File.class)
        .describedAs("File to write history to for incremental analysis");

    this.mutationThreshHoldSpec = parserAccepts(MUTATION_THRESHOLD)
        .withRequiredArg().ofType(Integer.class)
        .describedAs("Mutation score below which to throw an error")
        .defaultsTo(MUTATION_THRESHOLD.getDefault(Integer.class));

    this.coverageThreshHoldSpec = parserAccepts(COVERAGE_THRESHOLD)
        .withRequiredArg().ofType(Integer.class)
        .describedAs("Line coverage below which to throw an error")
        .defaultsTo(COVERAGE_THRESHOLD.getDefault(Integer.class));

    this.mutationEngine = parserAccepts(MUTATION_ENGINE).withRequiredArg()
        .ofType(String.class).describedAs("mutation engine to use")
        .defaultsTo(MUTATION_ENGINE.getDefault(String.class));

    this.javaExecutable = parserAccepts(JVM_PATH).withRequiredArg()
        .ofType(String.class).describedAs("path to java executable");

  }

  private OptionSpecBuilder parserAccepts(final ConfigOption option) {
    return this.parser.accepts(option.getParamName());
  }

  public ParseResult parse(final String[] args) {

    final ReportOptions data = new ReportOptions();
    try {
      final OptionSet userArgs = this.parser.parse(args);

      if (userArgs.has(this.projectFileSpec)) {
        return loadProjectFile(userArgs);
      } else {
        return parseCommandLine(data, userArgs);
      }
    } catch (final OptionException uoe) {
      return new ParseResult(data, uoe.getLocalizedMessage());
    }

  }

  /**
   * Creates a new ParseResult object using the command line arguments.
   * 
   * @param data
   *          the ReportOptions to populate.
   * @param userArgs
   *          the OptionSet which contains the command line arguments.
   * @return a new ParseResult, correctly configured using the command line
   *         arguments.
   */
  private ParseResult parseCommandLine(final ReportOptions data,
      final OptionSet userArgs) {
    data.setReportDir(userArgs.valueOf(this.reportDirSpec));
    data.setTargetClasses(FCollection.map(
        this.targetClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setTargetTests(FCollection.map(this.targetTestsSpec.values(userArgs),
        Glob.toGlobPredicate()));
    data.setSourceDirs(this.sourceDirSpec.values(userArgs));
    data.setMutators(this.mutators.values(userArgs));
    data.setDependencyAnalysisMaxDistance(this.depth.value(userArgs));
    data.addChildJVMArgs(this.jvmArgs.values(userArgs));

    data.setMutateStaticInitializers(userArgs.has(this.mutateStatics)
        && userArgs.valueOf(this.mutateStatics));

    data.setDetectInlinedCode(userArgs.has(this.detectInlinedCode)
        && userArgs.valueOf(this.detectInlinedCode));

    data.setIncludeLaunchClasspath(userArgs
        .valueOf(this.includeLaunchClasspathSpec));

    data.setShouldCreateTimestampedReports(userArgs
        .valueOf(this.timestampedReportsSpec));
    data.setNumberOfThreads(this.threadsSpec.value(userArgs));
    data.setTimeoutFactor(this.timeoutFactorSpec.value(userArgs));
    data.setTimeoutConstant(this.timeoutConstSpec.value(userArgs));
    data.setLoggingClasses(this.avoidCallsSpec.values(userArgs));
    data.setExcludedMethods(FCollection.map(
        this.excludedMethodsSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setExcludedClasses(FCollection.map(
        this.excludedClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setMaxMutationsPerClass(this.maxMutationsPerClassSpec.value(userArgs));
    data.setVerbose(userArgs.has(this.verboseSpec)
        && userArgs.valueOf(this.verboseSpec));

    data.addOutputFormats(this.outputFormatSpec.values(userArgs));
    data.setFailWhenNoMutations(this.failWhenNoMutations.value(userArgs));
    data.setCodePaths(this.codePaths.values(userArgs));
    data.setMutationUnitSize(this.mutationUnitSizeSpec.value(userArgs));

    data.setHistoryInputLocation(this.historyInputSpec.value(userArgs));
    data.setHistoryOutputLocation(this.historyOutputSpec.value(userArgs));
    data.setMutationThreshold(this.mutationThreshHoldSpec.value(userArgs));
    data.setCoverageThreshold(this.coverageThreshHoldSpec.value(userArgs));
    data.setMutationEngine(this.mutationEngine.value(userArgs));

    data.setExportLineCoverage(userArgs.has(this.exportLineCoverageSpec)
        && userArgs.valueOf(this.exportLineCoverageSpec));

    setClassPath(userArgs, data);

    setTestGroups(userArgs, data);
    data.setJavaExecutable(this.javaExecutable.value(userArgs));

    if (userArgs.has("?")) {
      return new ParseResult(data, "See above for supported parameters.");
    } else {
      return new ParseResult(data, null);
    }
  }

  private void setClassPath(final OptionSet userArgs, final ReportOptions data) {

    final List<String> elements = new ArrayList<String>();
    if (data.isIncludeLaunchClasspath()) {
      elements.addAll(ClassPath.getClassPathElementsAsPaths());
    } else {
      elements.addAll(FCollection.filter(ClassPath.getClassPathElementsAsPaths(),
              dependencyFilter));
    }
    elements.addAll(userArgs.valuesOf(this.additionalClassPathSpec));
    data.setClassPathElements(elements);
  }

  private void setTestGroups(final OptionSet userArgs,
      final ReportOptions data) {
    final TestGroupConfig conf = new TestGroupConfig(
        this.excludedGroupsSpec.values(userArgs),
        this.includedGroupsSpec.values(userArgs));

    data.setGroupConfig(conf);
  }

  /**
   * Creates a new ParseResult object, using the project file specified by the
   * user on the command line.
   * 
   * @param userArgs
   *          the OptionSet that contains all of the command line arguments.
   * @return a correctly instantiated ParseResult using the project file to load
   *         arguments.
   */
  private ParseResult loadProjectFile(final OptionSet userArgs) {
    try {
      final ProjectConfigurationParser configParser = ProjectConfigurationParserFactory
          .createParser();

      final ReportOptions loaded = configParser.loadProject(userArgs
          .valueOf(this.projectFileSpec));

      return new ParseResult(loaded, null);
    } catch (final ProjectConfigurationParserException e) {
      return new ParseResult(new ReportOptions(), "Project File ERROR: "
          + e.getMessage() + ".");
    } catch (final ProjectConfigurationException e) {
      return new ParseResult(new ReportOptions(), "Project File ERROR: "
          + e.getMessage() + ".");
    }
  }

  public void printHelp() {
    try {
      this.parser.printHelpOn(System.out);
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}
