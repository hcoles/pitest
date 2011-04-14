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
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.predicate.Predicate;

public class OptionsParserTest {

  private OptionsParser testee;

  @Before
  public void setUp() {
    this.testee = new OptionsParser();
  }

  @Test
  public void shouldParseReportDir() {

    final String value = "foo";
    final ReportOptions actual = parse("--reportDir", value);
    assertEquals(value, actual.getReportDir());
  }

  @Test
  public void shouldCreatePredicateFromCommaSeperatedListOfTargetClassGlobs() {
    final ReportOptions actual = parse("--targetClasses", "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetClassesFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  @Test
  public void shouldCreatePredicateFromCommaSeperatedListOfInScopeClassGlobs() {
    final ReportOptions actual = parse("--inScopeClasses", "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getClassesInScopeFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  @Test
  public void shouldParseCommaSeperatedListOfSourceDirectories() {
    final ReportOptions actual = parse("--sourceDirs", "foo/bar,bar/far");
    assertEquals(Arrays.asList(new File("foo/bar"), new File("bar/far")),
        actual.getSourceDirs());
  }

  @Test
  public void shouldParseMaxDepenencyDistance() {
    final ReportOptions actual = parse("--dependencyDistance", "42");
    assertEquals(42, actual.getDependencyAnalysisMaxDistance());
  }

  @Test
  public void shouldParseCommaSeperatedListOfJVMArgs() {
    final ReportOptions actual = parse("--jvmArgs", "foo,bar");
    assertEquals(Arrays.asList("foo", "bar"), actual.getJvmArgs());
  }

  @Test
  public void shouldParseCommaSeperatedListOfMutationOperators() {
    final ReportOptions actual = parse("--mutations",
        Mutator.CONDITIONALS_BOUNDARY.name() + "," + Mutator.MATH.name());
    assertEquals(Arrays.asList(Mutator.CONDITIONALS_BOUNDARY, Mutator.MATH),
        actual.getMutators());
  }

  @Test
  public void shouldDetermineIfMutateStaticInitializersFlagIsSet() {
    final ReportOptions actual = parse("--mutateStaticInits");
    assertTrue(actual.isMutateStaticInitializers());
  }

  @Test
  public void shouldParseNumberOfThreads() {
    final ReportOptions actual = parse("--threads", "42");
    assertEquals(42, actual.getNumberOfThreads());
  }

  @Test
  public void shouldDetermineIfIncludeJarFilesFlagIsSet() {
    final ReportOptions actual = parse("--includeJarFiles");
    assertTrue(actual.isIncludeJarFiles());
  }

  @Test
  public void shouldParseTimeOutFactor() {
    final ReportOptions actual = parse("--timeoutFactor", "1.32");
    assertEquals(1.32f, actual.getTimeoutFactor(), 0.1);
  }

  @Test
  public void shouldParseTimeOutConstant() {
    final ReportOptions actual = parse("--timeoutConst", "42");
    assertEquals(42, actual.getTimeoutConstant());
  }

  @Test
  public void shouldParseCommaSeperatedListOfTargetTestClassGlobs() {
    final ReportOptions actual = parse("--targetTest", "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  @Test
  public void shouldDefaultLoggingPackagesToDefaultsDefinedByDefaultMutationConfigFactory() {
    final ReportOptions actual = parse();
    assertEquals(DefaultMutationConfigFactory.LOGGING_CLASSES,
        actual.getLoggingClasses());
  }

  @Test
  public void shouldParseCommaSeperatedListOfLoggingPackages() {
    final ReportOptions actual = parse("--loggingClasses", "foo,bar,foo.bar");
    assertEquals(Arrays.asList("foo", "bar", "foo.bar"),
        actual.getLoggingClasses());
  }

  private ReportOptions parse(final String... args) {
    return this.testee.parse(args);
  }

}
