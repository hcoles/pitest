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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.Prelude;
import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.config.ConfigOption;
import org.pitest.mutationtest.engine.gregor.Mutator;
import org.pitest.mutationtest.report.OutputFormat;

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
    assertEquals(
        Arrays.asList(Mutator.CONDITIONALS_BOUNDARY.name(), Mutator.MATH.name()),
        actual.getMutators());
  }

  @Test
  public void shouldDetermineIfMutateStaticInitializersFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutateStaticInits");
    assertTrue(actual.isMutateStaticInitializers());
  }

  @Test
  public void shouldDetermineIfMutateStaticInitializersFlagIsSetWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutateStaticInits",
        "true");
    assertTrue(actual.isMutateStaticInitializers());
  }

  @Test
  public void shouldDetermineIfMutateStaticInitializersFlagIsSetWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutateStaticInits=false");
    assertFalse(actual.isMutateStaticInitializers());
  }

  @Test
  public void shouldNotCreateMutationsInStaticInitializerByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs("");
    assertFalse(actual.isMutateStaticInitializers());
  }

  @Test
  public void shouldNotDetectInlinedCodeByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs("");
    assertFalse(actual.isDetectInlinedCode());
  }

  @Test
  public void shouldDetermineIfInlinedCodeFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--detectInlinedCode");
    assertTrue(actual.isDetectInlinedCode());
  }

  @Test
  public void shouldDetermineIfInlinedCodeFlagIsSetWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--detectInlinedCode=false");
    assertFalse(actual.isDetectInlinedCode());
  }

  @Test
  public void shouldCreateTimestampedReportsByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertTrue(actual.shouldCreateTimeStampedReports());
  }

  @Test
  public void shouldDetermineIfSuppressTimestampedReportsFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--timestampedReports");
    assertTrue(actual.shouldCreateTimeStampedReports());
  }

  @Test
  public void shouldDetermineIfSuppressTimestampedReportsFlagIsSetWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--timestampedReports=false");
    assertFalse(actual.shouldCreateTimeStampedReports());
  }

  @Test
  public void shouldParseNumberOfThreads() {
    final ReportOptions actual = parseAddingRequiredArgs("--threads", "42");
    assertEquals(42, actual.getNumberOfThreads());
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
  public void shouldUseTargetClassesFilterForTestsWhenNoTargetTestsFilterSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--targetClasses",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
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
    assertEquals(ReportOptions.LOGGING_CLASSES,
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

  @Test
  public void shouldDefaultToHtmlReportWhenNoOutputFormatsSpecified() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertEquals(new HashSet<OutputFormat>(Arrays.asList(OutputFormat.HTML)),
        actual.getOutputFormats());
  }

  @Test
  public void shouldParseCommaSeperatedListOfOutputFormatsWhenSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--outputFormats",
        "HTML,CSV");
    assertEquals(
        new HashSet<OutputFormat>(Arrays.asList(OutputFormat.HTML,
            OutputFormat.CSV)), actual.getOutputFormats());
  }

  @Test
  public void shouldAcceptCommaSeperatedListOfAdditionalClassPathElements() {
    final ReportOptions ro = parseAddingRequiredArgs("--classPath",
        "/foo/bar,./boo");
    final Collection<String> actual = ro.getClassPathElements();
    assertTrue(actual.contains("/foo/bar"));
    assertTrue(actual.contains("./boo"));
  }

  @Test
  public void shouldDetermineIfFailWhenNoMutationsFlagIsSet() {
    assertTrue(parseAddingRequiredArgs("--failWhenNoMutations", "true")
        .shouldFailWhenNoMutations());
    assertFalse(parseAddingRequiredArgs("--failWhenNoMutations", "false")
        .shouldFailWhenNoMutations());
  }

  @Test
  public void shouldFailWhenNoMutationsSetByDefault() {
    assertTrue(parseAddingRequiredArgs("").shouldFailWhenNoMutations());
  }

  @Test
  public void shouldParseComaSeperatedListOfMutableCodePaths() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutableCodePaths",
        "foo,bar");
    assertEquals(Arrays.asList("foo", "bar"), actual.getCodePaths());
  }

  @Test
  public void shouldParseComaSeperatedListOfExcludedTestGroups() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--excludedTestNGGroups", "foo,bar");
    assertEquals(Arrays.asList("foo", "bar"), actual.getGroupConfig()
        .getExcludedGroups());
  }

  @Test
  public void shouldParseComaSeperatedListOfIncludedTestGroups() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--includedTestNGGroups", "foo,bar");
    assertEquals(Arrays.asList("foo", "bar"), actual.getGroupConfig()
        .getIncludedGroups());
  }

  @Test
  public void shouldParseMutationUnitSize() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutationUnitSize",
        "50");
    assertEquals(50, actual.getMutationUnitSize());
  }

  @Test
  public void shouldDefaultMutationUnitSizeToCorrectValue() {
    final ReportOptions actual = parseAddingRequiredArgs("");
    assertEquals(
        (int) ConfigOption.MUTATION_UNIT_SIZE.getDefault(Integer.class),
        actual.getMutationUnitSize());
  }

  @Test
  public void shouldDefaultToNoHistory() {
    final ReportOptions actual = parseAddingRequiredArgs("");
    assertNull(actual.getHistoryInputLocation());
    assertNull(actual.getHistoryOutputLocation());
  }

  @Test
  public void shouldParseHistoryInputLocation() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--historyInputLocation", "foo");
    assertEquals(new File("foo"), actual.getHistoryInputLocation());
  }

  @Test
  public void shouldParseHistoryOutputLocation() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--historyOutputLocation", "foo");
    assertEquals(new File("foo"), actual.getHistoryOutputLocation());
  }

  @Test
  public void shouldParseMutationThreshold() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--mutationThreshold", "42");
    assertEquals(42, actual.getMutationThreshold());
  }
  
  @Test
  public void shouldDefaultToGregorEngineWhenNoOptionSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertEquals("gregor", actual.getMutationEngine());
  }
  
  @Test
  public void shouldParseMutationEnigne() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--mutationEngine", "foo");
    assertEquals("foo", actual.getMutationEngine());
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
