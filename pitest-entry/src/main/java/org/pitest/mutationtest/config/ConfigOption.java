/*
 * Copyright 2011 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 ("the "License"");
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

import java.io.Serializable;

import org.pitest.mutationtest.build.PercentAndConstantTimeoutStrategy;

public enum ConfigOption {

  /**
   * The directory to write report sot
   */
  REPORT_DIR("reportDir"),
  /**
   * Filter defining classes to mutate
   */
  TARGET_CLASSES("targetClasses"),

  /**
   * Directories to examine to find source files to annotate when generating
   * report
   */
  SOURCE_DIR("sourceDirs"),
  /**
   * Mutation operations to use
   */
  MUTATIONS("mutators"),
  /**
   * Features to enable/disable
   */
  FEATURES("features"),
  /**
   * Maximum number of hops from a mutable class to a test
   */
  DEPENDENCY_DISTANCE("dependencyDistance", -1),
  /**
   * Arguments to launch child processes with
   */
  CHILD_JVM("jvmArgs"),
  /**
   * Do/don't mutate static initializers (slow as new ClassLoader required for
   * each mutant)
   */
  MUTATE_STATIC_INITIALIZERS("mutateStaticInits", false),

  /**
   * Do/don't create timestamped folders for reports
   */
  TIME_STAMPED_REPORTS("timestampedReports", true),

  /**
   * Number of threads to use
   */
  THREADS("threads", 1),
  /**
   * Multiple of normal runtime to allow before considering a mutation to have
   * timed out
   */
  TIMEOUT_FACTOR("timeoutFactor",
      PercentAndConstantTimeoutStrategy.DEFAULT_FACTOR),
  /**
   * Consant addiotnal period of time to allow before considering a mutation to
   * have timed out
   */
  TIMEOUT_CONST("timeoutConst",
      PercentAndConstantTimeoutStrategy.DEFAULT_CONSTANT),
  /**
   * Filter limiting tests to be considered
   */
  TEST_FILTER("targetTests"),
  /**
   * List of classes no to mutate lines of calls that contain call to
   */
  AVOID_CALLS("avoidCallsTo"),
  /**
   * Filter of methods not to be mutated
   */
  EXCLUDED_METHOD("excludedMethods"),
  /**
   * Maximum number of mutations to allow per class
   */
  MAX_MUTATIONS_PER_CLASS("maxMutationsPerClass", 0),
  /**
   * Flag to indicate if verbose logging should be enabled
   */
  VERBOSE("verbose", false),
  /**
   * Filter defining classes to exclude (both tests and mutees)
   */
  EXCLUDED_CLASSES("excludedClasses"),
  /**
   * Formats in which to output results
   */
  OUTPUT_FORMATS("outputFormats"),

  /**
   * Classpath entries to analyse. Although classes on the launch classpath will
   * also be analysed, this is the preferred place to specify the code to
   * analyse
   */
  CLASSPATH("classPath"),
   /**
   * Same as classPath above, but in a file. The file should contain paths to the jars
   * to be added to the classpath. one path per line.
   * This is usually only needed if you are running on windows and have a huge classpath
   */
  CLASSPATH_FILE("classPathFile"),
  /**
   * Flag to indicate if an error should be thrown if no mutations found
   */
  FAIL_WHEN_NOT_MUTATIONS("failWhenNoMutations", true),
  /**
   * Filter defining paths that should be treated as containing mutable code
   */
  CODE_PATHS("mutableCodePaths"),
  /**
   * TestNG groups/JUnit categories to include
   */
  INCLUDED_GROUPS("includedGroups"),
  /**
   * TestNG groupsJUnit categories to exclude
   */
  EXCLUDED_GROUPS("excludedGroups"),
  /**
   * Maximum number of mutations to include within a single unit of analysis.
   */
  MUTATION_UNIT_SIZE("mutationUnitSize", 0),

  /**
   * Do/don't attempt to detect inlined code from finally blocks
   */
  USE_INLINED_CODE_DETECTION("detectInlinedCode", true),

  /**
   * Location to read history from for incremental analysis
   */
  HISTORY_INPUT_LOCATION("historyInputLocation"),

  /**
   * Location to write history to for incremental analysis
   */
  HISTORY_OUTPUT_LOCATION("historyOutputLocation"),

  /**
   * Mutation score below which to throw an error
   */
  MUTATION_THRESHOLD("mutationThreshold", 0),

  /**
   * Number of surviving mutants at which to throw an error
   */
  MAX_SURVIVING("maxSurviving", -1),

  /**
   * Line coverage score below which to throw an error
   */
  COVERAGE_THRESHOLD("coverageThreshold", 0),

  /**
   * Mutation engine to use
   */
  MUTATION_ENGINE("mutationEngine", "gregor"),

  /**
   * Dump per test line coverage to disk
   */
  EXPORT_LINE_COVERAGE("exportLineCoverage", false),

  /**
   * Include launch classpath in analysis
   */
  INCLUDE_LAUNCH_CLASSPATH("includeLaunchClasspath", true),

  /**
   * Path to executable with which to run tests
   */
  JVM_PATH("jvmPath"),

  /**
   * Custom properties for plugins
   */
  PLUGIN_CONFIGURATION("pluginConfiguration");

  private final String       text;
  private final Serializable defaultValue;

  ConfigOption(final String text) {
    this(text, null);
  }

  ConfigOption(final String text, final Serializable defaultValue) {
    this.text = text;
    this.defaultValue = defaultValue;
  }

  public String getParamName() {
    return this.text;
  }

  @SuppressWarnings("unchecked")
  public <T> T getDefault(final Class<T> type) {
    // so much for type safety
    return (T) this.defaultValue;
  }

  @Override
  public String toString() {
    return this.text;
  }

}
