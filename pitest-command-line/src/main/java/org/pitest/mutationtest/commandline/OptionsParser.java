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

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;
import joptsimple.util.KeyValuePair;
import org.pitest.classpath.ClassPath;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.config.ConfigOption;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.Glob;
import org.pitest.util.Log;
import org.pitest.util.Unchecked;
import org.pitest.util.Verbosity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import java.util.function.Predicate;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static org.pitest.functional.Streams.asStream;
import static org.pitest.mutationtest.config.ConfigOption.ARG_LINE;
import static org.pitest.mutationtest.config.ConfigOption.AVOID_CALLS;
import static org.pitest.mutationtest.config.ConfigOption.CHILD_JVM;
import static org.pitest.mutationtest.config.ConfigOption.CLASSPATH;
import static org.pitest.mutationtest.config.ConfigOption.CLASSPATH_FILE;
import static org.pitest.mutationtest.config.ConfigOption.CODE_PATHS;
import static org.pitest.mutationtest.config.ConfigOption.COVERAGE_THRESHOLD;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_GROUPS;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_METHOD;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_RUNNERS;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_TEST_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.EXPORT_LINE_COVERAGE;
import static org.pitest.mutationtest.config.ConfigOption.FAIL_WHEN_NOT_MUTATIONS;
import static org.pitest.mutationtest.config.ConfigOption.FEATURES;
import static org.pitest.mutationtest.config.ConfigOption.FULL_MUTATION_MATRIX;
import static org.pitest.mutationtest.config.ConfigOption.HISTORY_INPUT_LOCATION;
import static org.pitest.mutationtest.config.ConfigOption.HISTORY_OUTPUT_LOCATION;
import static org.pitest.mutationtest.config.ConfigOption.INCLUDED_GROUPS;
import static org.pitest.mutationtest.config.ConfigOption.INCLUDED_TEST_METHODS;
import static org.pitest.mutationtest.config.ConfigOption.INCLUDE_LAUNCH_CLASSPATH;
import static org.pitest.mutationtest.config.ConfigOption.INPUT_ENCODING;
import static org.pitest.mutationtest.config.ConfigOption.JVM_PATH;
import static org.pitest.mutationtest.config.ConfigOption.MAX_MUTATIONS_PER_CLASS;
import static org.pitest.mutationtest.config.ConfigOption.MAX_SURVIVING;
import static org.pitest.mutationtest.config.ConfigOption.MUTATIONS;
import static org.pitest.mutationtest.config.ConfigOption.MUTATION_ENGINE;
import static org.pitest.mutationtest.config.ConfigOption.MUTATION_THRESHOLD;
import static org.pitest.mutationtest.config.ConfigOption.MUTATION_UNIT_SIZE;
import static org.pitest.mutationtest.config.ConfigOption.OUTPUT_ENCODING;
import static org.pitest.mutationtest.config.ConfigOption.OUTPUT_FORMATS;
import static org.pitest.mutationtest.config.ConfigOption.PLUGIN_CONFIGURATION;
import static org.pitest.mutationtest.config.ConfigOption.PROJECT_BASE;
import static org.pitest.mutationtest.config.ConfigOption.REPORT_DIR;
import static org.pitest.mutationtest.config.ConfigOption.SKIP_FAILING_TESTS;
import static org.pitest.mutationtest.config.ConfigOption.SOURCE_DIR;
import static org.pitest.mutationtest.config.ConfigOption.TARGET_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.TEST_FILTER;
import static org.pitest.mutationtest.config.ConfigOption.TEST_PLUGIN;
import static org.pitest.mutationtest.config.ConfigOption.TEST_STRENGTH_THRESHOLD;
import static org.pitest.mutationtest.config.ConfigOption.THREADS;
import static org.pitest.mutationtest.config.ConfigOption.TIMEOUT_CONST;
import static org.pitest.mutationtest.config.ConfigOption.TIMEOUT_FACTOR;
import static org.pitest.mutationtest.config.ConfigOption.TIME_STAMPED_REPORTS;
import static org.pitest.mutationtest.config.ConfigOption.USE_CLASSPATH_JAR;
import static org.pitest.mutationtest.config.ConfigOption.USE_INLINED_CODE_DETECTION;
import static org.pitest.mutationtest.config.ConfigOption.VERBOSE;
import static org.pitest.mutationtest.config.ConfigOption.VERBOSITY;

public class OptionsParser {

  private final Predicate<String>                    dependencyFilter;

  private static final Logger LOG = Log.getLogger();

  private final OptionParser                         parser;
  private final ArgumentAcceptingOptionSpec<String>  reportDirSpec;
  private final OptionSpec<String>                   targetClassesSpec;
  private final OptionSpec<String>                   targetTestsSpec;
  private final OptionSpec<String>                   avoidCallsSpec;
  private final OptionSpec<Integer>                  threadsSpec;
  private final OptionSpec<File>                     sourceDirSpec;
  private final OptionSpec<File>                     historyOutputSpec;
  private final OptionSpec<File>                     historyInputSpec;
  private final OptionSpec<String>                   mutators;
  private final OptionSpec<String>                   features;
  private final OptionSpec<String>                   argLine;
  private final OptionSpec<String>                   jvmArgs;
  private final CommaAwareArgsProcessor              jvmArgsProcessor;
  private final OptionSpec<Float>                    timeoutFactorSpec;
  private final OptionSpec<Long>                     timeoutConstSpec;
  private final OptionSpec<String>                   excludedMethodsSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> verboseSpec;
  private final ArgumentAcceptingOptionSpec<String>  verbositySpec;
  private final OptionSpec<String>                   excludedClassesSpec;
  private final OptionSpec<String>                   excludedTestClassesSpec;
  private final OptionSpec<String>                   outputFormatSpec;
  private final OptionSpec<String>                   additionalClassPathSpec;
  private final OptionSpec<File>                     classPathFile;
  private final ArgumentAcceptingOptionSpec<Boolean> failWhenNoMutations;
  private final ArgumentAcceptingOptionSpec<Boolean> skipFailingTests;
  private final ArgumentAcceptingOptionSpec<String>  codePaths;

  private final OptionSpec<String>                   excludedRunnersSpec;
  private final OptionSpec<String>                   excludedGroupsSpec;
  private final OptionSpec<String>                   includedGroupsSpec;
  private final OptionSpec<String>                   includedTestMethodsSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> fullMutationMatrixSpec;
  private final OptionSpec<Integer>                  mutationUnitSizeSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> timestampedReportsSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> detectInlinedCode;
  private final ArgumentAcceptingOptionSpec<Integer> mutationThreshHoldSpec;
  private final ArgumentAcceptingOptionSpec<Integer> testStrengthThreshHoldSpec;
  private final ArgumentAcceptingOptionSpec<Integer> coverageThreshHoldSpec;
  private final ArgumentAcceptingOptionSpec<Integer> maxSurvivingSpec;
  private final OptionSpec<String>                   mutationEngine;
  private final ArgumentAcceptingOptionSpec<Boolean> exportLineCoverageSpec;
  private final OptionSpec<String>                   javaExecutable;
  private final OptionSpec<KeyValuePair>             pluginPropertiesSpec;
  private final OptionSpec<String>                   testPluginSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> includeLaunchClasspathSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> useClasspathJarSpec;
  private final OptionSpec<File>                     projectBaseSpec;
  private final OptionSpec<String>                   inputEncoding;
  private final OptionSpec<String>                   outputEncoding;

  public OptionsParser(Predicate<String> dependencyFilter) {

    this.dependencyFilter = dependencyFilter;

    this.parser = new OptionParser();
    this.parser.acceptsAll(Arrays.asList("h", "?"), "show help");

    this.testPluginSpec = parserAccepts(TEST_PLUGIN)
        .withRequiredArg()
        .ofType(String.class)
        .defaultsTo("junit")
        .describedAs("this parameter is ignored and will be removed in a future release");

    this.reportDirSpec = parserAccepts(REPORT_DIR).withRequiredArg()
        .describedAs("directory to create report folder in").required();

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

    this.threadsSpec = parserAccepts(THREADS).withRequiredArg()
        .ofType(Integer.class).defaultsTo(THREADS.getDefault(Integer.class))
        .describedAs("number of threads to use for testing");

    parserAccepts(MAX_MUTATIONS_PER_CLASS)
        .withRequiredArg().ofType(Integer.class)
        .defaultsTo(MAX_MUTATIONS_PER_CLASS.getDefault(Integer.class))
        .describedAs("No longer supported. Use CLASSLIMIT(limit[42]) feature instead");

    this.sourceDirSpec = parserAccepts(SOURCE_DIR).withRequiredArg()
        .ofType(File.class).withValuesSeparatedBy(',')
        .describedAs("comma separated list of source directories").required();

    this.mutators = parserAccepts(MUTATIONS).withRequiredArg()
        .ofType(String.class).withValuesSeparatedBy(',')
        .describedAs("comma separated list of mutation operators");

    this.features = parserAccepts(FEATURES).withRequiredArg()
        .ofType(String.class).withValuesSeparatedBy(',')
        .describedAs("comma separated list of features to enable/disable.");

    this.argLine = parserAccepts(ARG_LINE).withRequiredArg()
            .describedAs("argline for child JVMs");

    this.jvmArgs = parserAccepts(CHILD_JVM).withRequiredArg()
        .describedAs("comma separated list of child JVM args");

    this.jvmArgsProcessor = new CommaAwareArgsProcessor(jvmArgs);

    this.detectInlinedCode = parserAccepts(USE_INLINED_CODE_DETECTION)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(USE_INLINED_CODE_DETECTION.getDefault(Boolean.class))
        .describedAs(
            "whether or not to try and detect code inlined from finally blocks");

    this.timestampedReportsSpec = parserAccepts(TIME_STAMPED_REPORTS)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(TIME_STAMPED_REPORTS.getDefault(Boolean.class))
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
            "comma separated list of globs for classes to exclude when mutating");

    this.excludedTestClassesSpec = parserAccepts(EXCLUDED_TEST_CLASSES)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma separated list of globs of test classes to exclude");

    this.verboseSpec = parserAccepts(VERBOSE)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(VERBOSE.getDefault(Boolean.class))
        .describedAs("whether or not to generate verbose output");

    this.verbositySpec = parserAccepts(VERBOSITY).withOptionalArg()
            .ofType(String.class).defaultsTo("DEFAULT")
            .describedAs("the verbosity of output");

    this.exportLineCoverageSpec = parserAccepts(EXPORT_LINE_COVERAGE)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(EXPORT_LINE_COVERAGE.getDefault(Boolean.class))
        .describedAs(
            "whether or not to dump per test line coverage data to disk");

    this.useClasspathJarSpec = parserAccepts(USE_CLASSPATH_JAR)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(USE_CLASSPATH_JAR.getDefault(Boolean.class))
        .describedAs("support large classpaths by creating a classpath jar");

    this.includeLaunchClasspathSpec = parserAccepts(INCLUDE_LAUNCH_CLASSPATH)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(INCLUDE_LAUNCH_CLASSPATH.getDefault(Boolean.class))
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

    this.classPathFile = this.parserAccepts(CLASSPATH_FILE).withRequiredArg()
        .ofType(File.class).describedAs("File with a list of additional classpath elements (one per line)");

    this.failWhenNoMutations = parserAccepts(FAIL_WHEN_NOT_MUTATIONS)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(FAIL_WHEN_NOT_MUTATIONS.getDefault(Boolean.class))
        .describedAs("whether to throw error if no mutations found");

    this.skipFailingTests = parserAccepts(SKIP_FAILING_TESTS)
        .withOptionalArg()
        .ofType(Boolean.class)
        .defaultsTo(SKIP_FAILING_TESTS.getDefault(Boolean.class))
        .describedAs("whether to ignore failing tests when computing coverage");

    this.codePaths = parserAccepts(CODE_PATHS)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "Globs identifying classpath roots containing mutable code");

    this.excludedRunnersSpec = parserAccepts(EXCLUDED_RUNNERS).withRequiredArg()
            .ofType(String.class).withValuesSeparatedBy(',')
            .describedAs("JUnit4 runners to exclude");

    this.includedGroupsSpec = parserAccepts(INCLUDED_GROUPS).withRequiredArg()
        .ofType(String.class).withValuesSeparatedBy(',')
        .describedAs("TestNG groups/JUnit categories to include");

    this.includedTestMethodsSpec = parserAccepts(INCLUDED_TEST_METHODS).withRequiredArg()
            .ofType(String.class).withValuesSeparatedBy(',')
            .describedAs("Test methods that should be included for challenging the mutants");

    this.excludedGroupsSpec = parserAccepts(EXCLUDED_GROUPS).withRequiredArg()
        .ofType(String.class).withValuesSeparatedBy(',')
        .describedAs("TestNG groups/JUnit categories to include");

    this.fullMutationMatrixSpec = parserAccepts(FULL_MUTATION_MATRIX)
        .withOptionalArg()
        .ofType(Boolean.class)
        .describedAs(
            "Whether to create a full mutation matrix")
        .defaultsTo(FULL_MUTATION_MATRIX.getDefault(Boolean.class));

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

    this.testStrengthThreshHoldSpec = parserAccepts(TEST_STRENGTH_THRESHOLD)
            .withRequiredArg().ofType(Integer.class)
            .describedAs("Test strength score below which to throw an error")
            .defaultsTo(TEST_STRENGTH_THRESHOLD.getDefault(Integer.class));

    this.maxSurvivingSpec = parserAccepts(MAX_SURVIVING)
        .withRequiredArg().ofType(Integer.class)
        .describedAs("Maximum number of surviving mutants to allow without throwing an error")
        .defaultsTo(MAX_SURVIVING.getDefault(Integer.class));

    this.coverageThreshHoldSpec = parserAccepts(COVERAGE_THRESHOLD)
        .withRequiredArg().ofType(Integer.class)
        .describedAs("Line coverage below which to throw an error")
        .defaultsTo(COVERAGE_THRESHOLD.getDefault(Integer.class));

    this.mutationEngine = parserAccepts(MUTATION_ENGINE).withRequiredArg()
        .ofType(String.class).describedAs("mutation engine to use")
        .defaultsTo(MUTATION_ENGINE.getDefault(String.class));

    this.javaExecutable = parserAccepts(JVM_PATH).withRequiredArg()
        .ofType(String.class).describedAs("path to java executable");

    this.inputEncoding = parserAccepts(INPUT_ENCODING).withRequiredArg()
            .ofType(String.class).describedAs("input encoding")
            .defaultsTo(INPUT_ENCODING.getDefault(String.class));

    this.outputEncoding = parserAccepts(OUTPUT_ENCODING).withRequiredArg()
            .ofType(String.class).describedAs("output encoding")
            .defaultsTo(OUTPUT_ENCODING.getDefault(String.class));

    this.pluginPropertiesSpec = parserAccepts(PLUGIN_CONFIGURATION)
        .withRequiredArg().ofType(KeyValuePair.class)
        .describedAs("custom plugin properties");

    this.projectBaseSpec = parserAccepts(PROJECT_BASE)
            .withRequiredArg().ofType(File.class);

  }

  private OptionSpecBuilder parserAccepts(final ConfigOption option) {
    return this.parser.accepts(option.getParamName());
  }

  public ParseResult parse(final String[] args) {
    final ReportOptions data = new ReportOptions();
    try {
      final OptionSet userArgs = this.parser.parse(args);
      return parseCommandLine(data, userArgs);

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
    data.setReportDir(this.reportDirSpec.value(userArgs));
    data.setTargetClasses(this.targetClassesSpec.values(userArgs));
    data.setTargetTests(asStream(this.targetTestsSpec.values(userArgs))
            .map(Glob.toGlobPredicate())
            .collect(Collectors.toList()));
    data.setSourceDirs(asPaths(this.sourceDirSpec.values(userArgs)));
    data.setMutators(this.mutators.values(userArgs));
    data.setFeatures(this.features.values(userArgs));

    data.setArgLine(this.argLine.value(userArgs));

    data.addChildJVMArgs(this.jvmArgsProcessor.values(userArgs));
    data.setFullMutationMatrix(
            (userArgs.has(this.fullMutationMatrixSpec) && !userArgs.hasArgument(this.fullMutationMatrixSpec))
                    || this.fullMutationMatrixSpec.value(userArgs));
    data.setDetectInlinedCode(
            (userArgs.has(this.detectInlinedCode) && !userArgs.hasArgument(this.detectInlinedCode))
                    || this.detectInlinedCode.value(userArgs));
    data.setIncludeLaunchClasspath(
            (userArgs.has(this.includeLaunchClasspathSpec) && !userArgs.hasArgument(this.includeLaunchClasspathSpec))
                    || this.includeLaunchClasspathSpec.value(userArgs));
    data.setUseClasspathJar(
            (userArgs.has(this.useClasspathJarSpec) && !userArgs.hasArgument(this.useClasspathJarSpec))
                    || this.useClasspathJarSpec.value(userArgs));
    data.setShouldCreateTimestampedReports(
            (userArgs.has(this.timestampedReportsSpec) && !userArgs.hasArgument(this.timestampedReportsSpec))
                    || this.timestampedReportsSpec.value(userArgs));
    data.setNumberOfThreads(this.threadsSpec.value(userArgs));
    data.setTimeoutFactor(this.timeoutFactorSpec.value(userArgs));
    data.setTimeoutConstant(this.timeoutConstSpec.value(userArgs));
    data.setLoggingClasses(this.avoidCallsSpec.values(userArgs));
    data.setExcludedMethods(this.excludedMethodsSpec.values(userArgs));
    data.setExcludedClasses(this.excludedClassesSpec.values(userArgs));
    data.setExcludedTestClasses(asStream(this.excludedTestClassesSpec.values(userArgs)).
            map(Glob.toGlobPredicate())
            .collect(Collectors.toList()));
    configureVerbosity(data, userArgs);

    data.addOutputFormats(this.outputFormatSpec.values(userArgs));
    data.setFailWhenNoMutations(
            (userArgs.has(this.failWhenNoMutations) && !userArgs.hasArgument(this.failWhenNoMutations))
                    || this.failWhenNoMutations.value(userArgs));
    data.setSkipFailingTests(
            (userArgs.has(this.skipFailingTests) && !userArgs.hasArgument(this.skipFailingTests))
                    || this.skipFailingTests.value(userArgs));
    data.setCodePaths(this.codePaths.values(userArgs));
    data.setMutationUnitSize(this.mutationUnitSizeSpec.value(userArgs));
    data.setHistoryInputLocation(this.historyInputSpec.value(userArgs));
    data.setHistoryOutputLocation(this.historyOutputSpec.value(userArgs));
    data.setMutationThreshold(this.mutationThreshHoldSpec.value(userArgs));
    data.setTestStrengthThreshold(this.testStrengthThreshHoldSpec.value(userArgs));
    data.setMaximumAllowedSurvivors(this.maxSurvivingSpec.value(userArgs));
    data.setCoverageThreshold(this.coverageThreshHoldSpec.value(userArgs));
    data.setMutationEngine(this.mutationEngine.value(userArgs));
    data.setFreeFormProperties(listToProperties(this.pluginPropertiesSpec.values(userArgs)));
    data.setExportLineCoverage(
            (userArgs.has(this.exportLineCoverageSpec) && !userArgs.hasArgument(this.exportLineCoverageSpec))
                    || this.exportLineCoverageSpec.value(userArgs));

    setClassPath(userArgs, data);

    setTestGroups(userArgs, data);

    data.setExcludedRunners(this.excludedRunnersSpec.values(userArgs));

    data.setIncludedTestMethods(this.includedTestMethodsSpec.values(userArgs));
    data.setJavaExecutable(this.javaExecutable.value(userArgs));

    setEncoding(data, userArgs);

    if (userArgs.has(projectBaseSpec)) {
      data.setProjectBase(this.projectBaseSpec.value(userArgs).toPath());
    }

    if (userArgs.has("?")) {
      return new ParseResult(data, "See above for supported parameters.");
    } else {
      return new ParseResult(data, null);
    }
  }

  private void setEncoding(ReportOptions data, OptionSet userArgs) {
    data.setInputEncoding(Charset.forName(this.inputEncoding.value(userArgs)));
    data.setOutputEncoding(Charset.forName(this.outputEncoding.value(userArgs)));
  }

  private void configureVerbosity(ReportOptions data, OptionSet userArgs) {
    boolean isVerbose = (userArgs.has(this.verboseSpec) && !userArgs.hasArgument(this.verboseSpec))
            || this.verboseSpec.value(userArgs);
    if (isVerbose) {
      data.setVerbosity(Verbosity.VERBOSE);
    } else {
      data.setVerbosity(Verbosity.fromString(this.verbositySpec.value(userArgs)));
    }

  }

  private void setClassPath(final OptionSet userArgs, final ReportOptions data) {

    final List<String> elements = new ArrayList<>();
    if (data.isIncludeLaunchClasspath()) {
      elements.addAll(ClassPath.getClassPathElementsAsPaths());
    } else {
      elements.addAll(FCollection.filter(
          ClassPath.getClassPathElementsAsPaths(), this.dependencyFilter));
    }
    if (userArgs.has(this.classPathFile)) {
      try (BufferedReader classPathFileBR = new BufferedReader(new FileReader(this.classPathFile.value(userArgs).getAbsoluteFile()))) {
        String element;
        while ((element = classPathFileBR.readLine()) != null) {
          elements.add(element);
        }
      } catch (final IOException ioe) {
        LOG.warning("Unable to read class path file:" + this.classPathFile.value(userArgs).getAbsolutePath() + " - "
                + ioe.getMessage());
      }
      data.setUseClasspathJar(true);
    }
    elements.addAll(this.additionalClassPathSpec.values(userArgs));
    data.setClassPathElements(elements);
  }

  private void setTestGroups(final OptionSet userArgs, final ReportOptions data) {
    final TestGroupConfig conf = new TestGroupConfig(
        this.excludedGroupsSpec.values(userArgs),
        this.includedGroupsSpec.values(userArgs));

    data.setGroupConfig(conf);
  }

  private Properties listToProperties(List<KeyValuePair> kvps) {
    final Properties p = new Properties();
    for (final KeyValuePair kvp : kvps) {
      p.put(kvp.key, kvp.value);
    }
    return p;
  }

  private Collection<Path> asPaths(List<File> values) {
    return values.stream()
            .map(File::toPath)
            .collect(Collectors.toList());
  }

  public void printHelp() {
    try {
      this.parser.printHelpOn(System.out);
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }


}
