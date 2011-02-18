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
import org.pitest.util.Glob;
import org.pitest.util.Unchecked;

public class OptionsParser {

  private static final String               TEST_CENTRIC                   = "testCentric";
  private final static String               REPORT_DIR_ARG                 = "reportDir";
  private final static String               TARGET_CLASSES_ARG             = "targetClasses";
  private final static String               IN_SCOPE_CLASSES_ARG           = "inScopeClasses";
  private final static String               SOURCE_DIR_ARG                 = "sourceDirs";
  private final static String               MUTATIONS_ARG                  = "mutations";
  private final static String               DEPENDENCY_DISTANCE_ARG        = "dependencyDistance";
  private final static String               CHILD_JVM_ARGS                 = "jvmArgs";
  private final static String               MUTATE_STATIC_INITIALIZERS_ARG = "mutateStaticInits";
  private final static String               THREADS_ARG                    = "threads";

  private final OptionParser                parser;
  final ArgumentAcceptingOptionSpec<String> reportDirSpec;
  final OptionSpec<String>                  targetClassesSpec;
  final OptionSpec<String>                  inScopeClassesSpec;
  final OptionSpec<Integer>                 depth;
  final OptionSpec<Integer>                 threadsSpec;
  final OptionSpec<File>                    sourceDirSpec;
  final OptionSpec<Mutator>                 mutators;
  final OptionSpec<String>                  jvmArgs;
  final OptionSpecBuilder                   mutateStatics;

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
  }

  public ReportOptions parse(final String[] args) {

    final OptionSet userArgs = this.parser.parse(args);

    final ReportOptions data = new ReportOptions();

    data.setReportDir(userArgs.valueOf(this.reportDirSpec));
    data.setTargetClasses(FCollection.map(
        this.targetClassesSpec.values(userArgs), Glob.toGlobPredicate()));
    data.setSourceDirs(this.sourceDirSpec.values(userArgs));
    data.setMutators(this.mutators.values(userArgs));
    data.setDependencyAnalysisMaxDistance(this.depth.value(userArgs));
    data.setValid(validateArgs(userArgs));
    data.setShowHelp(userArgs.has("?"));
    data.setIsTestCentric(userArgs.has(TEST_CENTRIC));
    data.addChildJVMArgs(this.jvmArgs.values(userArgs));
    data.setMutateStaticInitializers(userArgs.has(this.mutateStatics));
    data.setNumberOfThreads(this.threadsSpec.value(userArgs));

    return data;

  }

  private static boolean validateArgs(final OptionSet userArgs) {
    return userArgs.has(REPORT_DIR_ARG) && userArgs.has(TARGET_CLASSES_ARG)
        && userArgs.has(SOURCE_DIR_ARG);
  }

  public void printHelp() {
    try {
      this.parser.printHelpOn(System.out);
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}
