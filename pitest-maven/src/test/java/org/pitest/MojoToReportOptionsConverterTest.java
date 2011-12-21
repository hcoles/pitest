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
package org.pitest;

import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;

import org.pitest.functional.Prelude;
import org.pitest.functional.predicate.Predicate;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.mutationtest.DefaultMutationConfigFactory;
import org.pitest.mutationtest.Mutator;
import org.pitest.mutationtest.ReportOptions;
import org.pitest.mutationtest.report.OutputFormat;
import org.pitest.testng.TestNGConfiguration;
import org.pitest.util.Unchecked;

public class MojoToReportOptionsConverterTest extends BasePitMojoTest {

  private MojoToReportOptionsConverter testee;

  public void testsParsesReportDir() {
    final ReportOptions actual = parseConfig("<reportsDirectory>Foo</reportsDirectory>");
    assertEquals(new File("Foo").getAbsolutePath(), actual.getReportDir());
  }

  public void testCreatesPredicateFromListOfTargetClassGlobs() {
    final String xml = "<targetClasses>" + //
        "                     <param>foo*</param>" + //
        "                     <param>bar*</param>" + //
        "                  </targetClasses>";

    final ReportOptions actual = parseConfig(xml);
    final Predicate<String> actualPredicate = actual.getTargetClassesFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  public void testCreatesPredicateFromListOfInScopeClassGlobs() {
    final String xml = "<inScopeClasses>" + //
        "                      <param>foo*</param>" + //
        "                      <param>bar*</param>" + //
        "                  </inScopeClasses>";

    final ReportOptions actual = parseConfig(xml);
    final Predicate<String> actualPredicate = actual.getClassesInScopeFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  public void testUsesSourceDirectoriesFromProject() {
    when(this.project.getCompileSourceRoots()).thenReturn(Arrays.asList("src"));
    when(this.project.getTestCompileSourceRoots()).thenReturn(
        Arrays.asList("tst"));
    final ReportOptions actual = parseConfig("");
    assertEquals(Arrays.asList(new File("src"), new File("tst")),
        actual.getSourceDirs());
  }

  public void testParsesMaxDepenencyDistance() {
    final ReportOptions actual = parseConfig("<maxDependencyDistance>42</maxDependencyDistance>");
    assertEquals(42, actual.getDependencyAnalysisMaxDistance());
  }

  public void testParsesListOfJVMArgs() {
    final String xml = "<jvmArgs>" + //
        "                      <param>foo</param>" + //
        "                      <param>bar</param>" + //
        "                  </jvmArgs>";
    final ReportOptions actual = parseConfig(xml);
    assertEquals(Arrays.asList("foo", "bar"), actual.getJvmArgs());
  }

  public void testParsesListOfMutationOperators() {
    final String xml = "<mutators>" + //
        "                      <param>CONDITIONALS_BOUNDARY</param>" + //
        "                      <param>MATH</param>" + //
        "                  </mutators>";
    final ReportOptions actual = parseConfig(xml);
    assertEquals(
        Mutator.asCollection(Mutator.CONDITIONALS_BOUNDARY, Mutator.MATH),
        actual.getMutators());
  }

  public void testParsersMutateStaticInitializersFlag() {
    assertTrue(parseConfig(
        "<mutateStaticInitializers>true</mutateStaticInitializers>")
        .isMutateStaticInitializers());
    assertFalse(parseConfig(
        "<mutateStaticInitializers>false</mutateStaticInitializers>")
        .isMutateStaticInitializers());
  }

  public void testParsesNumberOfThreads() {
    final ReportOptions actual = parseConfig("<threads>42</threads>");
    assertEquals(42, actual.getNumberOfThreads());
  }

  public void testParsesTimeOutFactor() {
    final ReportOptions actual = parseConfig("<timeoutFactor>1.32</timeoutFactor>");
    assertEquals(1.32f, actual.getTimeoutFactor(), 0.1);
  }

  public void testParsesTimeOutConstant() {
    final ReportOptions actual = parseConfig("<timeoutConstant>42</timeoutConstant>");
    assertEquals(42, actual.getTimeoutConstant());
  }

  public void testParsesListOfTargetTestClassGlobs() {
    final String xml = "<targetTests>" + //
        "                      <param>foo*</param>" + //
        "                      <param>bar*</param>" + //
        "                  </targetTests>";
    final ReportOptions actual = parseConfig(xml);
    final Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  public void testParsesListOfExcludedClassGlobsAndApplyTheseToInScopeClasses() {
    final String xml = "<excludedClasses>" + //
        "                      <param>foo*</param>" + //
        "                  </excludedClasses>" + //
        "                  <inScopeClasses>" + //
        "                      <param>foo*</param>" + //
        "                      <param>bar*</param>" + //
        "                  </inScopeClasses>";
    final ReportOptions actual = parseConfig(xml);
    final Predicate<String> actualPredicate = actual.getClassesInScopeFilter();
    assertFalse(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
  }

  public void testParsesListOfExcludedClassGlobsAndApplyTheseToTests() {
    final String xml = "<excludedClasses>" + //
        "                      <param>foo*</param>" + //
        "                  </excludedClasses>" + //
        "                  <targetTests>" + //
        "                      <param>foo*</param>" + //
        "                      <param>bar*</param>" + //
        "                  </targetTests>";
    final ReportOptions actual = parseConfig(xml);
    final Predicate<String> testPredicate = actual.getTargetTestsFilter();
    assertFalse(testPredicate.apply("foo_anything"));
    assertTrue(testPredicate.apply("bar_anything"));
  }

  public void testParsesListOfExcludedClassGlobsAndApplyTheseToTargets() {
    final String xml = "<excludedClasses>" + //
        "                      <param>foo*</param>" + //
        "                  </excludedClasses>" + //
        "                  <targetClasses>" + //
        "                      <param>foo*</param>" + //
        "                      <param>bar*</param>" + //
        "                  </targetClasses>";
    final ReportOptions actual = parseConfig(xml);
    final Predicate<String> targetPredicate = actual.getTargetClassesFilter();
    assertFalse(targetPredicate.apply("foo_anything"));
    assertTrue(targetPredicate.apply("bar_anything"));
  }

  public void testDefaultsLoggingPackagesToDefaultsDefinedByDefaultMutationConfigFactory() {
    final ReportOptions actual = parseConfig("");
    assertEquals(DefaultMutationConfigFactory.LOGGING_CLASSES,
        actual.getLoggingClasses());
  }

  public void testParsesListOfClassesToAvoidCallTo() {
    final String xml = "<avoidCallsTo>" + //
        "                      <param>foo</param>" + //
        "                      <param>bar</param>" + //
        "                      <param>foo.bar</param>" + //
        "                  </avoidCallsTo>";
    final ReportOptions actual = parseConfig(xml);
    assertEquals(Arrays.asList("foo", "bar", "foo.bar"),
        actual.getLoggingClasses());
  }

  public void testParsesCommaListOfExcludedMethods() {
    final String xml = "<excludedMethods>" + //
        "                      <param>foo*</param>" + //
        "                      <param>bar*</param>" + //
        "                      <param>car</param>" + //
        "                  </excludedMethods>";
    final ReportOptions actual = parseConfig(xml);
    final Predicate<String> actualPredicate = Prelude.or(actual
        .getExcludedMethods());
    assertTrue(actualPredicate.apply("foox"));
    assertTrue(actualPredicate.apply("barx"));
    assertTrue(actualPredicate.apply("car"));
    assertFalse(actualPredicate.apply("carx"));
  }

  public void testParsesVerboseFlag() {
    assertTrue(parseConfig("<verbose>true</verbose>").isVerbose());
    assertFalse(parseConfig("<verbose>false</verbose>").isVerbose());
  }

  public void testDefaultsToHtmlReportWhenNoOutputFormatsSpecified() {
    final ReportOptions actual = parseConfig("");
    assertEquals(new HashSet<OutputFormat>(Arrays.asList(OutputFormat.HTML)),
        actual.getOutputFormats());
  }

  public void testParsesListOfOutputFormatsWhenSupplied() {
    final String xml = "<outputFormats>" + //
        "                      <param>HTML</param>" + //
        "                      <param>CSV</param>" + //
        "                  </outputFormats>";
    final ReportOptions actual = parseConfig(xml);
    assertEquals(
        new HashSet<OutputFormat>(Arrays.asList(OutputFormat.HTML,
            OutputFormat.CSV)), actual.getOutputFormats());
  }

  public void testParsesFailWhenNotMutations() {
    assertTrue(parseConfig("<failWhenNoMutations>true</failWhenNoMutations>")
        .shouldFailWhenNoMutations());
    assertFalse(parseConfig("<failWhenNoMutations>false</failWhenNoMutations>")
        .shouldFailWhenNoMutations());
  }

  public void testParsesTestTypeToUse() {
    assertTrue(parseConfig("<testType>TESTNG</testType>")
        .createCoverageOptions().getPitConfig() instanceof TestNGConfiguration);
    assertTrue(parseConfig("<testType>JUNIT</testType>")
        .createCoverageOptions().getPitConfig() instanceof JUnitCompatibleConfiguration);
  }

  private ReportOptions parseConfig(final String xml) {
    try {
      final String pom = createPomWithConfiguration(xml);
      final PitMojo mojo = createPITMojo(pom);
      this.testee = new MojoToReportOptionsConverter(mojo);
      final ReportOptions actual = this.testee.convert();
      return actual;
    } catch (final Exception ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}
