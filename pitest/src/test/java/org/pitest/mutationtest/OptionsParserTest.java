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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.Prelude;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.commandline.OptionsParser;

public class OptionsParserTest {

  private OptionsParser testee;

  @Before
  public void setUp() {
    this.testee = new OptionsParser();
  }

  @Test
  public void shouldParseReportDir() {
    final String value = "foo";
    final ReportOptions actual = parseAddingRequiredArgs("--reportDir", value);
    assertEquals(value, actual.getReportDir());
  }

  @Test
  public void shouldCreatePredicateFromCommaSeperatedListOfTargetClassGlobs() {
    final ReportOptions actual = parseAddingRequiredArgs("--targetClasses",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetClassesFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  @Test
  public void shouldCreatePredicateFromCommaSeperatedListOfInScopeClassGlobs() {
    final ReportOptions actual = parseAddingRequiredArgs("--inScopeClasses",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getClassesInScopeFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  @Test
  public void shouldParseCommaSeperatedListOfSourceDirectories() {
    final ReportOptions actual = parseAddingRequiredArgs("--sourceDirs",
        "foo/bar,bar/far");
    assertEquals(Arrays.asList(new File("foo/bar"), new File("bar/far")),
        actual.getSourceDirs());
  }

  @Test
  public void shouldParseMaxDepenencyDistance() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--dependencyDistance", "42");
    assertEquals(42, actual.getDependencyAnalysisMaxDistance());
  }

  @Test
  public void shouldParseCommaSeperatedListOfJVMArgs() {
    final ReportOptions actual = parseAddingRequiredArgs("--jvmArgs", "foo,bar");
    assertEquals(Arrays.asList("foo", "bar"), actual.getJvmArgs());
  }

  @Test
  public void shouldParseCommaSeperatedListOfMutationOperators() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutators",
        Mutator.CONDITIONALS_BOUNDARY.name() + "," + Mutator.MATH.name());
    assertEquals(Arrays.asList(Mutator.CONDITIONALS_BOUNDARY, Mutator.MATH),
        actual.getMutators());
  }

  @Test
  public void shouldDetermineIfMutateStaticInitializersFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutateStaticInits");
    assertTrue(actual.isMutateStaticInitializers());
  }

  @Test
  public void shouldParseNumberOfThreads() {
    final ReportOptions actual = parseAddingRequiredArgs("--threads", "42");
    assertEquals(42, actual.getNumberOfThreads());
  }

  @Test
  public void shouldDetermineIfIncludeJarFilesFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--includeJarFiles");
    assertTrue(actual.isIncludeJarFiles());
  }

  @Test
  public void shouldParseTimeOutFactor() {
    final ReportOptions actual = parseAddingRequiredArgs("--timeoutFactor",
        "1.32");
    assertEquals(1.32f, actual.getTimeoutFactor(), 0.1);
  }

  @Test
  public void shouldParseTimeOutConstant() {
    final ReportOptions actual = parseAddingRequiredArgs("--timeoutConst", "42");
    assertEquals(42, actual.getTimeoutConstant());
  }

  @Test
  public void shouldParseCommaSeperatedListOfTargetTestClassGlobs() {
    final ReportOptions actual = parseAddingRequiredArgs("--targetTest",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  @Test
  public void shouldParseCommaSperatedListOfExcludedClassGlobsAndApplyTheseToInScopeClasses() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedClasses",
        "foo*", "--inScopeClasses", "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getClassesInScopeFilter();
    assertFalse(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
  }

  @Test
  public void shouldParseCommaSperatedListOfExcludedClassGlobsAndApplyTheseToTests() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedClasses",
        "foo*", "--targetTests", "foo*,bar*", "--targetClasses", "foo*,bar*");
    final Predicate<String> testPredicate = actual.getTargetTestsFilter();
    assertFalse(testPredicate.apply("foo_anything"));
    assertTrue(testPredicate.apply("bar_anything"));
  }

  @Test
  public void shouldParseCommaSperatedListOfExcludedClassGlobsAndApplyTheseToTargets() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedClasses",
        "foo*", "--targetTests", "foo*,bar*", "--targetClasses", "foo*,bar*");

    final Predicate<String> targetPredicate = actual.getTargetClassesFilter();
    assertFalse(targetPredicate.apply("foo_anything"));
    assertTrue(targetPredicate.apply("bar_anything"));
  }

  @Test
  public void shouldDefaultLoggingPackagesToDefaultsDefinedByDefaultMutationConfigFactory() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertEquals(DefaultMutationConfigFactory.LOGGING_CLASSES,
        actual.getLoggingClasses());
  }

  @Test
  public void shouldParseCommaSeperatedListOfClassesToAvoidCallTo() {
    final ReportOptions actual = parseAddingRequiredArgs("--avoidCallsTo",
        "foo,bar,foo.bar");
    assertEquals(Arrays.asList("foo", "bar", "foo.bar"),
        actual.getLoggingClasses());
  }

  @Test
  public void shouldParseCommaSeperatedListOfExcludedMethods() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedMethods",
        "foo*,bar*,car");
    final Predicate<String> actualPredicate = Prelude.or(actual
        .getExcludedMethods());
    assertTrue(actualPredicate.apply("foox"));
    assertTrue(actualPredicate.apply("barx"));
    assertTrue(actualPredicate.apply("car"));
    assertFalse(actualPredicate.apply("carx"));
  }

  @Test
  public void shouldParseVerboseFlag() {
    final ReportOptions actual = parseAddingRequiredArgs("--verbose");
    assertTrue(actual.isVerbose());
  }

  private ReportOptions parseAddingRequiredArgs(final String... args) {

    final List<String> a = new ArrayList<String>();
    a.addAll(Arrays.asList(args));
    addIfNotPresent(a, "--targetClasses");
    addIfNotPresent(a, "--reportDir");
    addIfNotPresent(a, "--sourceDirs");
    return parse(a.toArray(new String[a.size()]));
  }

  private void addIfNotPresent(final List<String> uniqueArgs, final String value) {
    if (!uniqueArgs.contains(value)) {
      uniqueArgs.add(value);
      uniqueArgs.add("ignore");
    }
  }

  private ReportOptions parse(final String... args) {
    return this.testee.parse(args).getOptions();
  }

}
