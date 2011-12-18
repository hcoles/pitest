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
import static org.pitest.mutationtest.config.ConfigOption.DEPENDENCY_DISTANCE;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.EXCLUDED_METHOD;
import static org.pitest.mutationtest.config.ConfigOption.FAIL_WHEN_NOT_MUTATIONS;
import static org.pitest.mutationtest.config.ConfigOption.INCLUDE_JAR_FILES;
import static org.pitest.mutationtest.config.ConfigOption.IN_SCOPE_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.MAX_MUTATIONS_PER_CLASS;
import static org.pitest.mutationtest.config.ConfigOption.MUTATE_STATIC_INITIALIZERS;
import static org.pitest.mutationtest.config.ConfigOption.MUTATIONS;
import static org.pitest.mutationtest.config.ConfigOption.OUTPUT_FORMATS;
import static org.pitest.mutationtest.config.ConfigOption.PROJECT_FILE;
import static org.pitest.mutationtest.config.ConfigOption.REPORT_DIR;
import static org.pitest.mutationtest.config.ConfigOption.SOURCE_DIR;
import static org.pitest.mutationtest.config.ConfigOption.TARGET_CLASSES;
import static org.pitest.mutationtest.config.ConfigOption.TEST_FILTER;
import static org.pitest.mutationtest.config.ConfigOption.THREADS;
import static org.pitest.mutationtest.config.ConfigOption.TIMEOUT_CONST;
import static org.pitest.mutationtest.config.ConfigOption.TIMEOUT_FACTOR;
import static org.pitest.mutationtest.config.ConfigOption.VERBOSE;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

import org.pitest.functional.FCollection;
import org.pitest.mutationtest.Mutator;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.config.ConfigOption;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.project.ProjectConfigurationException;
import org.pitest.project.ProjectConfigurationParser;
import org.pitest.project.ProjectConfigurationParserException;
import org.pitest.project.ProjectConfigurationParserFactory;
import org.pitest.util.Glob;
import org.pitest.util.Unchecked;

public class OptionsParser {

  private final OptionParser                         parser;
  private final ArgumentAcceptingOptionSpec<String>  reportDirSpec;
  private final OptionSpec<String>                   targetClassesSpec;
  private final OptionSpec<String>                   targetTestsSpec;
  private final OptionSpec<String>                   inScopeClassesSpec;
  private final OptionSpec<String>                   avoidCallsSpec;
  private final OptionSpec<Integer>                  depth;
  private final OptionSpec<Integer>                  threadsSpec;
  private final OptionSpec<File>                     sourceDirSpec;
  private final OptionSpec<Mutator>                  mutators;
  private final OptionSpec<String>                   jvmArgs;
  private final OptionSpecBuilder                    mutateStatics;
  private final OptionSpecBuilder                    includeJarFilesSpec;
  private final OptionSpec<Float>                    timeoutFactorSpec;
  private final OptionSpec<Long>                     timeoutConstSpec;
  private final OptionSpec<String>                   excludedMethodsSpec;
  private final OptionSpec<Integer>                  maxMutationsPerClassSpec;
  private final OptionSpecBuilder                    verboseSpec;
  private final OptionSpec<String>                   excludedClassesSpec;
  private final OptionSpec<OutputFormat>             outputFormatSpec;
  private final OptionSpec<String>                   projectFileSpec;
  private final OptionSpec<String>                   additionalClassPathSpec;
  private final ArgumentAcceptingOptionSpec<Boolean> failWhenNoMutations;

  public OptionsParser() {
    this.parser = new OptionParser();
    this.parser.acceptsAll(Arrays.asList("h", "?"), "show help");

    this.reportDirSpec = parserAccepts(REPORT_DIR).withRequiredArg()
        .describedAs("directory to create report folder in").required();

    this.projectFileSpec = parserAccepts(PROJECT_FILE).withRequiredArg()
        .ofType(String.class)
        .describedAs("The name of the project file to use.");

    this.targetClassesSpec = parserAccepts(TARGET_CLASSES)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of filters to match against classes to test")
        .required();

    this.avoidCallsSpec = parserAccepts(AVOID_CALLS)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of packages to consider as untouchable logging calls");

    this.targetTestsSpec = parserAccepts(TEST_FILTER)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of filters to match against tests to run");

    this.inScopeClassesSpec = parserAccepts(IN_SCOPE_CLASSES)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of filter to match against classes to consider in scope");

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
        .describedAs("comma seperated list of source directories").required();

    this.mutators = parserAccepts(MUTATIONS).withRequiredArg()
        .ofType(Mutator.class).withValuesSeparatedBy(',')
        .describedAs("comma seperated list of mutation operators")
        .defaultsTo(Mutator.DEFAULTS);

    this.jvmArgs = parserAccepts(CHILD_JVM).withRequiredArg()
        .withValuesSeparatedBy(',')
        .describedAs("comma seperated list of child JVM args");

    this.mutateStatics = parserAccepts(MUTATE_STATIC_INITIALIZERS);

    this.includeJarFilesSpec = parserAccepts(INCLUDE_JAR_FILES);

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
            "comma seperated list of filters to match against methods to exclude from mutation analysis");

    this.excludedClassesSpec = parserAccepts(EXCLUDED_CLASSES)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of globs fr classes to exclude when looking for both mutation target and tests");

    this.verboseSpec = parserAccepts(VERBOSE);

    this.outputFormatSpec = parserAccepts(OUTPUT_FORMATS)
        .withRequiredArg()
        .ofType(OutputFormat.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of formats in which to write output during the analysis pahse")
        .defaultsTo(OutputFormat.HTML);

    this.additionalClassPathSpec = parserAccepts(CLASSPATH).withRequiredArg()
        .ofType(String.class).withValuesSeparatedBy(',')
        .describedAs("coma seperated list of additional classpath elements");

    this.failWhenNoMutations = parserAccepts(FAIL_WHEN_NOT_MUTATIONS)
        .withRequiredArg().ofType(Boolean.class).defaultsTo(true)
        .describedAs("whether to throw error if no mutations found");
  }

  private OptionSpecBuilder parserAccepts(ConfigOption option) {
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
   * Creates a new {@see ParseResult} object using the command line arguments.
   * 
   * @param data
   *          the {@see ReportOptions} to populate.
   * @param userArgs
   *          the {@see OptionSet} which contains the command line arguments.
   * @return a new {@see ParseResult}, correctly configured using the command
   *         line arguments.
   */
  private ParseResult parseCommandLine(final ReportOptions data,
      final OptionSet userArgs) {
    data.setReportDir(userArgs.valueOf(this.reportDirSpec));
    data.setTargetClasses(FCollection.map(
        this.targetClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setClassesInScope(FCollection.map(
        this.inScopeClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setTargetTests(FCollection.map(this.targetTestsSpec.values(userArgs),
        Glob.toGlobPredicate()));
    data.setSourceDirs(this.sourceDirSpec.values(userArgs));
    data.setMutators(Mutator.asCollection(this.mutators.values(userArgs)));
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
    data.setMaxMutationsPerClass(this.maxMutationsPerClassSpec.value(userArgs));
    data.setVerbose(userArgs.has(this.verboseSpec));

    data.addOutputFormats(this.outputFormatSpec.values(userArgs));
    data.setFailWhenNoMutations(this.failWhenNoMutations.value(userArgs));

    setClassPath(userArgs, data);

    if (userArgs.has("?")) {
      return new ParseResult(data, "See above for supported parameters.");
    } else {
      return new ParseResult(data, null);
    }
  }

  private void setClassPath(final OptionSet userArgs, final ReportOptions data) {
    data.addClassPathElements(userArgs.valuesOf(this.additionalClassPathSpec));
  }

  /**
   * Creates a new {@see ParseResult} object, using the project file specified
   * by the user on the command line.
   * 
   * @param userArgs
   *          the {@see OptionSet} that contains all of the command line
   *          arguments.
   * @return a correctly instantiated {@see ParseResult} using the project file
   *         to load arguments.
   */
  private ParseResult loadProjectFile(final OptionSet userArgs) {
    try {
      final ProjectConfigurationParser parser = ProjectConfigurationParserFactory
          .createParser();

      final ReportOptions loaded = parser.loadProject(userArgs
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
