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

import org.pitest.mutationtest.instrument.PercentAndConstantTimeoutStrategy;

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
   * Maximum numbe of hops from a mutable class to a test
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
  NO_TIME_STAMPED_REPORTS("noTimestampedReports", false),
  
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
   * External config file path
   */
  PROJECT_FILE("configFile"),
  /**
   * Additional classpath entries to use
   */
  CLASSPATH("classPath"),
  /**
   * Flag to indicate if an error should be thrown if no mutations found
   */
  FAIL_WHEN_NOT_MUTATIONS("failWhenNoMutations", true),
  /**
   * Filter defining paths that should be treated as containing mutable code
   */
  CODE_PATHS("mutableCodePaths"),
  /**
   * TestNG groups to include
   */
  INCLUDED_GROUPS("includedTestNGGroups"),
  /**
   * TestNG groups to exclude
   */
  EXCLUDED_GROUPS("excludedTestNGGroups"),
  /**
   * Maximum number of mutations to include within a single unit of analysis.
   */
  MUTATION_UNIT_SIZE("mutationUnitSize", 0);

  private final String text;
  private final Object defaultValue;

  ConfigOption(final String text) {
    this(text, null);
  }

  ConfigOption(final String text, final Object defaultValue) {
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
