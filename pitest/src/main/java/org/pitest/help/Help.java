/*
 * Copyright 2011 Henry Coles
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
package org.pitest.help;

public enum Help {

  NO_MUTATIONS_FOUND(
      "No mutations found. This probably means there is an issue with either the supplied classpath or filters."), //
  WRONG_JUNIT_VERSION(
      "Unsupported JUnit version %s. PIT requires JUnit 4.6 or above."), //
  FAILING_TESTS(
      "All tests did not pass without mutation when calculating line coverage. Mutation testing requires a green suite."), //
  NO_JUNIT(
      "JUnit was not found on the classpath. PIT requires JUnit 4.6 or above."), //
  NO_SOURCE_FILE(
      "The class %s does not contain source debug information. All classes must have an associated source file"), //
  NO_LINE_NUMBERS(
      "No classes found with line number debug information. All classes should be compiled with source and line number debug information."), //
  NO_TEST_LIBRARY(
      "No test library found on classpath. PIT requires either JUnit 4 (but can run JUnit 3 tests) or TestNG"), //
  BAD_FILTER(
      "The supplied filter would cause PIT to try and mutate itself. This will lead to many wasted hours of confusion and debugging. You have better things to do with your life so please don't do this again."), //
  UNKNOWN_MUTATOR(
      "Mutator or group %s is unknown. Check PIT configuration and try again."),
  NO_JUNIT_EXCLUDE_RUNNERS(
      "Exclude Runners option requires JUnit 4.6 or above.");
  
  private static final String URL = "http://pitest.org";
  private final String        text;

  Help(final String text) {
    this.text = text;
  }

  @Override
  public String toString() {
    return this.text + getLink();
  }

  private String getLink() {
    return "\nSee " + URL + " for more details.";
  }

  public String format(final Object... params) {
    return String.format(this.toString(), params);
  }

}
