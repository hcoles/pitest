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

import joptsimple.*;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.DefaultMutationConfigFactory;
import org.pitest.mutationtest.Mutator;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectFileParser;
import org.pitest.project.ProjectFileParserException;
import org.pitest.project.ProjectFileParserFactory;
import org.pitest.util.ClasspathUtil;
import org.pitest.util.Glob;
import org.pitest.util.Unchecked;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class OptionsParser {

  public final static String REPORT_DIR_ARG = "reportDir";
  public final static String TARGET_CLASSES_ARG = "targetClasses";
  public final static String IN_SCOPE_CLASSES_ARG = "inScopeClasses";
  public final static String SOURCE_DIR_ARG = "sourceDirs";
  public final static String MUTATIONS_ARG = "mutators";
  public final static String DEPENDENCY_DISTANCE_ARG = "dependencyDistance";
  public final static String CHILD_JVM_ARGS = "jvmArgs";
  public final static String MUTATE_STATIC_INITIALIZERS_ARG = "mutateStaticInits";
  public final static String THREADS_ARG = "threads";
  public final static String INCLUDE_JAR_FILES = "includeJarFiles";
  public final static String TIMEOUT_FACTOR_ARG = "timeoutFactor";
  public final static String TIMEOUT_CONST_ARG = "timeoutConst";
  public final static String TEST_FILTER_ARGS = "targetTests";
  public final static String AVOID_CALLS_ARG = "avoidCallsTo";
  public final static String EXCLUDED_METHOD_ARG = "excludedMethods";
  public final static String MAX_MUTATIONS_PER_CLASS_ARG = "maxMutationsPerClass";
  public final static String VERBOSE = "verbose";
  public final static String EXCLUDED_CLASSES_ARG = "excludedClasses";
  public final static String OUTPUT_FORMATS = "outputFormats";
  public final static String PROJECT_FILE = "projectFile";
  public final static String CLASSPATH_ARG = "classPath";

  private final OptionParser parser;
  private final ArgumentAcceptingOptionSpec<String> reportDirSpec;
  private final OptionSpec<String> targetClassesSpec;
  private final OptionSpec<String> targetTestsSpec;
  private final OptionSpec<String> inScopeClassesSpec;
  private final OptionSpec<String> avoidCallsSpec;
  private final OptionSpec<Integer> depth;
  private final OptionSpec<Integer> threadsSpec;
  private final OptionSpec<File> sourceDirSpec;
  private final OptionSpec<Mutator> mutators;
  private final OptionSpec<String> jvmArgs;
  private final OptionSpecBuilder mutateStatics;
  private final OptionSpecBuilder includeJarFilesSpec;
  private final OptionSpec<Float> timeoutFactorSpec;
  private final OptionSpec<Long> timeoutConstSpec;
  private final OptionSpec<String> excludedMethodsSpec;
  private final OptionSpec<Integer> maxMutationsPerClassSpec;
  private final OptionSpecBuilder verboseSpec;
  private final OptionSpec<String> excludedClassesSpec;
  private final OptionSpec<OutputFormat> outputFormatSpec;
  private final OptionSpec<String> projectFile;

  public OptionsParser() {
    this.parser = new OptionParser();
    this.parser.acceptsAll(Arrays.asList("h", "?"), "show help");

    this.reportDirSpec = this.parser.accepts(REPORT_DIR_ARG).withRequiredArg()
        .describedAs("directory to create report folder in").required();

    this.projectFile = this.parser
        .accepts(PROJECT_FILE)
        .withRequiredArg()
        .ofType(String.class)
        .describedAs("The name of the project file to use.");

    this.targetClassesSpec = this.parser
        .accepts(TARGET_CLASSES_ARG)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of filters to match against classes to test")
        .required();

    this.avoidCallsSpec = this.parser
        .accepts(AVOID_CALLS_ARG)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of packages to consider as untouchable logging calls");

    this.targetTestsSpec = this.parser
        .accepts(TEST_FILTER_ARGS)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of filters to match against tests to run");

    this.inScopeClassesSpec = this.parser
        .accepts(IN_SCOPE_CLASSES_ARG)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of filter to match against classes to consider in scope");

    this.depth = this.parser.accepts(DEPENDENCY_DISTANCE_ARG).withRequiredArg()
        .ofType(Integer.class).defaultsTo(-1)
        .describedAs("maximum distance to look from test for covered classes");

    this.threadsSpec = this.parser.accepts(THREADS_ARG).withRequiredArg()
        .ofType(Integer.class).defaultsTo(1)
        .describedAs("number of threads to use for testing");

    this.maxMutationsPerClassSpec = this.parser
        .accepts(MAX_MUTATIONS_PER_CLASS_ARG).withRequiredArg()
        .ofType(Integer.class).defaultsTo(0)
        .describedAs("max number of mutations to allow for each class");

    this.sourceDirSpec = this.parser.accepts(SOURCE_DIR_ARG).withRequiredArg()
        .ofType(File.class).withValuesSeparatedBy(',')
        .describedAs("comma seperated list of source directories").required();

    this.mutators = this.parser
        .accepts(MUTATIONS_ARG)
        .withRequiredArg()
        .ofType(Mutator.class)
        .withValuesSeparatedBy(',')
        .describedAs("comma seperated list of mutation operators")
        .defaultsTo(
            Mutator.MATH,
            DefaultMutationConfigFactory.DEFAULT_MUTATORS
                .toArray(new Mutator[]{}));

    this.jvmArgs = this.parser.accepts(CHILD_JVM_ARGS).withRequiredArg()
        .withValuesSeparatedBy(',')
        .describedAs("comma seperated list of child JVM args");

    this.mutateStatics = this.parser.accepts(MUTATE_STATIC_INITIALIZERS_ARG);

    this.includeJarFilesSpec = this.parser.accepts(INCLUDE_JAR_FILES);

    this.timeoutFactorSpec = this.parser
        .accepts(TIMEOUT_FACTOR_ARG)
        .withOptionalArg()
        .ofType(Float.class)
        .describedAs("factor to apply to calculate maximum test duration")
        .defaultsTo(
            Float.valueOf(PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR));

    this.timeoutConstSpec = this.parser.accepts(TIMEOUT_CONST_ARG)
        .withOptionalArg().ofType(Long.class)
        .describedAs("constant to apply to calculate maximum test duration")
        .defaultsTo(PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT);

    this.excludedMethodsSpec = this.parser
        .accepts(EXCLUDED_METHOD_ARG)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of filters to match against methods to exclude from mutation analysis");

    this.excludedClassesSpec = this.parser
        .accepts(EXCLUDED_CLASSES_ARG)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of globs fr classes to exclude when looking for both mutation target and tests");

    this.verboseSpec = this.parser.accepts(VERBOSE);

    this.outputFormatSpec = this.parser
        .accepts(OUTPUT_FORMATS)
        .withRequiredArg()
        .ofType(OutputFormat.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of formats in which to write output during the analysis pahse")
        .defaultsTo(OutputFormat.HTML);
  }

  public ParseResult parse(final String[] args) {

    final ReportOptions data = new ReportOptions();
    try {
      final OptionSet userArgs = this.parser.parse(args);

      if (userArgs.has(PROJECT_FILE)) {
        return loadProjectFile(userArgs);
      } else {
        return parseCommandLine(data, userArgs);
      }
    } catch (final OptionException uoe) {
      return new ParseResult(data, uoe.getLocalizedMessage());
    }

  }

  /**
   * Creates a new {@see ParseResult} object using the command line arguments.
   *
   * @param data     the {@see ReportOptions} to populate.
   * @param userArgs the {@see OptionSet} which contains the command line arguments.
   * @return a new {@see ParseResult}, correctly configured using the command line arguments.
   */
  private ParseResult parseCommandLine(ReportOptions data, OptionSet userArgs) {
    data.setReportDir(userArgs.valueOf(this.reportDirSpec));
    data.setTargetClasses(FCollection.map(
        this.targetClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setClassesInScope(FCollection.map(
        this.inScopeClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setTargetTests(FCollection.map(
        this.targetTestsSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setSourceDirs(this.sourceDirSpec.values(userArgs));
    data.setMutators(this.mutators.values(userArgs));
    data.setDependencyAnalysisMaxDistance(this.depth.value(userArgs));
    data.addChildJVMArgs(this.jvmArgs.values(userArgs));
    data.setMutateStaticInitializers(userArgs.has(this.mutateStatics));
    data.setNumberOfThreads(this.threadsSpec.value(userArgs));
    data.setIncludeJarFiles(userArgs.has(this.includeJarFilesSpec));
    data.setTimeoutFactor(this.timeoutFactorSpec.value(userArgs));
    data.setTimeoutConstant(this.timeoutConstSpec.value(userArgs));
    data.setLoggingClasses(this.avoidCallsSpec.values(userArgs));
    data.setExcludedMethods(FCollection.map(
        this.excludedMethodsSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setExcludedClasses(FCollection.map(
        this.excludedClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setMaxMutationsPerClass(this.maxMutationsPerClassSpec
        .value(userArgs));
    data.setVerbose(userArgs.has(this.verboseSpec));

    data.addOutputFormats(this.outputFormatSpec.values(userArgs));

    if (userArgs.has("?")) {
      return new ParseResult(data, "See above for supported parameters.");
    } else {
      return new ParseResult(data, null);
    }
  }

  /**
   * Creates a new {@see ParseResult} object, using the project file specified by the user on the command line.
   *
   * @param userArgs the {@see OptionSet} that contains all of the command line arguments.
   * @return a correctly instantiated {@see ParseResult} using the project file to load arguments.
   */
  private ParseResult loadProjectFile(OptionSet userArgs) {
    try {
      ProjectFileParser parser = ProjectFileParserFactory.createParser();

      File projectFile = new File((String) userArgs.valueOf(PROJECT_FILE));

      if (!projectFile.exists()) {
        throw new ProjectFileParserException("Cannot load project from file " + projectFile.getAbsolutePath() + " as it does not exist.");
      }

      if (!projectFile.isFile()) {
        throw new ProjectFileParserException("Cannot load project from file " + projectFile.getAbsolutePath() + " as it is a directory.");
      }

      if (!projectFile.canRead()) {
        throw new ProjectFileParserException("Cannot load project from file " + projectFile.getAbsolutePath() + " as it cannot be read.");
      }

      ReportOptions loaded = parser.loadProjectFile(new FileInputStream(projectFile));

      // as the process is already running, we need to add any additionally defined classpath elements
      // to the system classloader so they are available to the methods later on.
      for (String s : loaded.getClassPathElements()) {
        ClasspathUtil.addPath(s);
      }

      return new ParseResult(loaded, null);
    } catch (ProjectFileParserException e) {
      return new ParseResult(new ReportOptions(), "Project File ERROR: " + e.getMessage() + ".");
    } catch (ProjectConfigurationException e) {
      return new ParseResult(new ReportOptions(), "Project File ERROR: " + e.getMessage() + ".");
    } catch (FileNotFoundException e) {
      return new ParseResult(new ReportOptions(), "Project File ERROR: " + e.getMessage() + ".");
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
