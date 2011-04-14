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
package org.pitest.mutationtest;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import joptsimple.OptionSpecBuilder;

import org.pitest.functional.FCollection;
import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;
import org.pitest.util.Glob;
import org.pitest.util.Unchecked;

public class OptionsParser {

  private static final String                       TEST_CENTRIC                   = "testCentric";
  private final static String                       REPORT_DIR_ARG                 = "reportDir";
  private final static String                       TARGET_CLASSES_ARG             = "targetClasses";
  private final static String                       IN_SCOPE_CLASSES_ARG           = "inScopeClasses";
  private final static String                       SOURCE_DIR_ARG                 = "sourceDirs";
  private final static String                       MUTATIONS_ARG                  = "mutations";
  private final static String                       DEPENDENCY_DISTANCE_ARG        = "dependencyDistance";
  private final static String                       CHILD_JVM_ARGS                 = "jvmArgs";
  private final static String                       MUTATE_STATIC_INITIALIZERS_ARG = "mutateStaticInits";
  private final static String                       THREADS_ARG                    = "threads";
  private final static String                       INCLUDE_JAR_FILES              = "includeJarFiles";
  private final static String                       TIMEOUT_FACTOR_ARG             = "timeoutFactor";
  private final static String                       TIMEOUT_CONST_ARG              = "timeoutConst";
  private final static String                       TEST_FILTER_ARGS               = "targetTests";
  private final static String                       LOGGING_CLASSES_ARG            = "loggingClasses";

  private final OptionParser                        parser;
  private final ArgumentAcceptingOptionSpec<String> reportDirSpec;
  private final OptionSpec<String>                  targetClassesSpec;
  private final OptionSpec<String>                  targetTestsSpec;
  private final OptionSpec<String>                  inScopeClassesSpec;
  private final OptionSpec<String>                  loggingClassesSpec;
  private final OptionSpec<Integer>                 depth;
  private final OptionSpec<Integer>                 threadsSpec;
  private final OptionSpec<File>                    sourceDirSpec;
  private final OptionSpec<Mutator>                 mutators;
  private final OptionSpec<String>                  jvmArgs;
  private final OptionSpecBuilder                   mutateStatics;
  private final OptionSpecBuilder                   includeJarFilesSpec;
  private final OptionSpec<Float>                   timeoutFactorSpec;
  private final OptionSpec<Long>                    timeoutConstSpec;

  public OptionsParser() {
    this.parser = new OptionParser();
    this.parser.acceptsAll(Arrays.asList("h", "?"), "show help");

    this.parser.accepts("codeCentric");
    this.parser.accepts(TEST_CENTRIC);

    this.reportDirSpec = this.parser.accepts(REPORT_DIR_ARG).withRequiredArg()
        .describedAs("directory to create report folder in");

    this.targetClassesSpec = this.parser
        .accepts(TARGET_CLASSES_ARG)
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs(
            "comma seperated list of filter to match against classes to test");

    this.loggingClassesSpec = this.parser
        .accepts(LOGGING_CLASSES_ARG)
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
        .ofType(Integer.class).defaultsTo(4)
        .describedAs("maximum distance to look from test for covered classes");

    this.threadsSpec = this.parser.accepts(THREADS_ARG).withRequiredArg()
        .ofType(Integer.class).defaultsTo(1)
        .describedAs("number of threads to use for testing");

    this.sourceDirSpec = this.parser.accepts(SOURCE_DIR_ARG).withRequiredArg()
        .ofType(File.class).withValuesSeparatedBy(',')
        .describedAs("comma seperated list of source directories");

    this.mutators = this.parser
        .accepts(MUTATIONS_ARG)
        .withRequiredArg()
        .ofType(Mutator.class)
        .withValuesSeparatedBy(',')
        .describedAs("comma seperated list of mutation operators")
        .defaultsTo(
            Mutator.MATH,
            DefaultMutationConfigFactory.DEFAULT_MUTATORS
                .toArray(new Mutator[] {}));

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
  }

  public ReportOptions parse(final String[] args) {

    final OptionSet userArgs = this.parser.parse(args);

    final ReportOptions data = new ReportOptions();

    data.setReportDir(userArgs.valueOf(this.reportDirSpec));
    data.setTargetClasses(FCollection.map(
        this.targetClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setClassesInScope(FCollection.map(
        this.inScopeClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setTargetTests(FCollection.map(this.targetTestsSpec.values(userArgs),
        Glob.toGlobPredicate()));
    data.setSourceDirs(this.sourceDirSpec.values(userArgs));
    data.setMutators(this.mutators.values(userArgs));
    data.setDependencyAnalysisMaxDistance(this.depth.value(userArgs));
    data.setValid(validateArgs(userArgs));
    data.setShowHelp(userArgs.has("?"));
    data.addChildJVMArgs(this.jvmArgs.values(userArgs));
    data.setMutateStaticInitializers(userArgs.has(this.mutateStatics));
    data.setNumberOfThreads(this.threadsSpec.value(userArgs));
    data.setIncludeJarFiles(userArgs.has(this.includeJarFilesSpec));
    data.setTimeoutFactor(this.timeoutFactorSpec.value(userArgs));
    data.setTimeoutConstant(this.timeoutConstSpec.value(userArgs));
    data.setLoggingClasses(this.loggingClassesSpec.values(userArgs));

    return data;

  }

  private static boolean validateArgs(final OptionSet userArgs) {
    return userArgs.has(REPORT_DIR_ARG) && userArgs.has(TARGET_CLASSES_ARG)
        && userArgs.has(SOURCE_DIR_ARG);
  }

  protected void printHelp() {
    try {
      this.parser.printHelpOn(System.out);
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}
