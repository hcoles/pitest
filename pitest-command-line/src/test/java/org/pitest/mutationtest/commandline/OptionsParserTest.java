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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.config.ConfigOption;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;

public class OptionsParserTest {

  private static final String JAVA_PATH_SEPARATOR      = "/";

  private static final String JAVA_CLASS_PATH_PROPERTY = "java.class.path";

  private OptionsParser       testee;

  @Mock
  private Predicate<String>   filter;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    when(this.filter.apply(any(String.class))).thenReturn(true);
    this.testee = new OptionsParser(this.filter);
  }

  @Test
  public void shouldParseReportDir() {
    final String value = "foo";
    final ReportOptions actual = parseAddingRequiredArgs("--reportDir", value);
    assertEquals(value, actual.getReportDir());
  }

  @Test
  public void shouldCreatePredicateFromCommaSeparatedListOfTargetClassGlobs() {
    final ReportOptions actual = parseAddingRequiredArgs("--targetClasses",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetClassesFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  @Test
  public void shouldParseCommaSeparatedListOfSourceDirectories() {
    final ReportOptions actual = parseAddingRequiredArgs("--sourceDirs",
        "foo/bar,bar/far");
    assertEquals(Arrays.asList(new File("foo/bar"), new File("bar/far")), actual.getSourceDirs());
  }

  @Test
  public void shouldParseMaxDepenencyDistance() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--dependencyDistance", "42");
    assertEquals(42, actual.getDependencyAnalysisMaxDistance());
  }

  @Test
  public void shouldParseCommaSeparatedListOfJVMArgs() {
    final ReportOptions actual = parseAddingRequiredArgs("--jvmArgs", "foo,bar");
    assertEquals(Arrays.asList("foo", "bar"), actual.getJvmArgs());
  }

  @Test
  public void shouldParseCommaSeparatedListOfMutationOperators() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutators",
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR.name() + ","
            + MathMutator.MATH_MUTATOR.name());
    assertEquals(Arrays.asList(
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY_MUTATOR.name(),
        MathMutator.MATH_MUTATOR.name()), actual.getMutators());
  }
  
  @Test
  public void shouldParseCommaSeparatedListOfFeatures() {
    final ReportOptions actual = parseAddingRequiredArgs("--features", "+FOO(),-BAR(value=1 & value=2)");
    assertThat(actual.getFeatures()).contains("+FOO()", "-BAR(value=1 & value=2)");
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
  public void shouldParseCommaSeparatedListOfTargetTestClassGlobs() {
    final ReportOptions actual = parseAddingRequiredArgs("--targetTest",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
  }

  @Test
  public void shouldParseCommaSeparatedListOfTargetTestClassGlobAsRegex() {
    ReportOptions actual = parseAddingRequiredArgs("--targetTest",
            "~foo\\w*,~bar.*");
    Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.apply("foo_anything"));
    assertTrue(actualPredicate.apply("bar_anything"));
    assertFalse(actualPredicate.apply("notfoobar"));
    actual = parseAddingRequiredArgs("--targetTest",
            "~.*?foo\\w*,~bar.*");
    actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.apply("notfoobar"));
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
  public void shouldParseCommaSeparatedListOfExcludedClassGlobsAndApplyTheseToTests() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedClasses",
        "foo*", "--targetTests", "foo*,bar*", "--targetClasses", "foo*,bar*");
    final Predicate<String> testPredicate = actual.getTargetTestsFilter();
    assertFalse(testPredicate.apply("foo_anything"));
    assertTrue(testPredicate.apply("bar_anything"));
  }

  @Test
  public void shouldParseCommaSeparatedListOfExcludedClassGlobsAndApplyTheseToTargets() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedClasses",
        "foo*", "--targetTests", "foo*,bar*", "--targetClasses", "foo*,bar*");

    final Predicate<String> targetPredicate = actual.getTargetClassesFilter();
    assertFalse(targetPredicate.apply("foo_anything"));
    assertTrue(targetPredicate.apply("bar_anything"));
  }

  @Test
  public void shouldDefaultLoggingPackagesToDefaultsDefinedByDefaultMutationConfigFactory() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertEquals(ReportOptions.LOGGING_CLASSES, actual.getLoggingClasses());
  }

  @Test
  public void shouldParseCommaSeparatedListOfClassesToAvoidCallTo() {
    final ReportOptions actual = parseAddingRequiredArgs("--avoidCallsTo",
        "foo,bar,foo.bar");
    assertEquals(Arrays.asList("foo", "bar", "foo.bar"),
        actual.getLoggingClasses());
  }

  @Test
  public void shouldParseCommaSeparatedListOfExcludedMethods() {
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
    assertEquals(new HashSet<String>(Arrays.asList("HTML")),
        actual.getOutputFormats());
  }

  @Test
  public void shouldParseCommaSeparatedListOfOutputFormatsWhenSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--outputFormats",
        "HTML,CSV");
    assertEquals(new HashSet<String>(Arrays.asList("HTML", "CSV")),
        actual.getOutputFormats());
  }

  @Test
  public void shouldAcceptCommaSeparatedListOfAdditionalClassPathElements() {
    final ReportOptions ro = parseAddingRequiredArgs("--classPath",
        "/foo/bar,./boo");
    final Collection<String> actual = ro.getClassPathElements();
    assertTrue(actual.contains("/foo/bar"));
    assertTrue(actual.contains("./boo"));
  }

  @Test
  public void shouldAcceptFileWithListOfAdditionalClassPathElements() {
    ClassLoader classLoader = getClass().getClassLoader();
    File classPathFile = new File(classLoader.getResource("testClassPathFile.txt").getFile());
    final ReportOptions ro = parseAddingRequiredArgs("--classPathFile",
	    classPathFile.getAbsolutePath());
    final Collection<String> actual = ro.getClassPathElements();
    assertTrue(actual.contains("C:/foo"));
    assertTrue(actual.contains("/etc/bar"));
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
  public void shouldParseCommaSeparatedListOfMutableCodePaths() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutableCodePaths",
        "foo,bar");
    assertEquals(Arrays.asList("foo", "bar"), actual.getCodePaths());
  }

  @Test
  public void shouldParseCommaSeparatedListOfExcludedTestGroups() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedGroups",
        "foo,bar");
    assertEquals(Arrays.asList("foo", "bar"), actual.getGroupConfig()
        .getExcludedGroups());
  }

  @Test
  public void shouldParseCommaSeparatedListOfIncludedTestGroups() {
    final ReportOptions actual = parseAddingRequiredArgs("--includedGroups",
        "foo,bar");
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
    final ReportOptions actual = parseAddingRequiredArgs("--mutationThreshold",
        "42");
    assertEquals(42, actual.getMutationThreshold());
  }
  
  @Test
  public void shouldParseMaximumAllowedSurvivingMutants() {
    final ReportOptions actual = parseAddingRequiredArgs("--maxSurviving",
        "42");
    assertEquals(42, actual.getMaximumAllowedSurvivors());
  }

  @Test
  public void shouldParseCoverageThreshold() {
    final ReportOptions actual = parseAddingRequiredArgs("--coverageThreshold",
        "42");
    assertEquals(42, actual.getCoverageThreshold());
  }

  @Test
  public void shouldDefaultToGregorEngineWhenNoOptionSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertEquals("gregor", actual.getMutationEngine());
  }

  @Test
  public void shouldParseMutationEnigne() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutationEngine",
        "foo");
    assertEquals("foo", actual.getMutationEngine());
  }

  @Test
  public void shouldDefaultJVMToNull() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertEquals(null, actual.getJavaExecutable());
  }

  @Test
  public void shouldParseJVM() {
    final ReportOptions actual = parseAddingRequiredArgs("--jvmPath", "foo");
    assertEquals("foo", actual.getJavaExecutable());
  }

  @Test
  public void shouldParseExportLineCoverageFlag() {
    final ReportOptions actual = parseAddingRequiredArgs("--exportLineCoverage");
    assertTrue(actual.shouldExportLineCoverage());
  }

  @Test
  public void shouldNotExportLineCoverageWhenFlagNotSet() {
    final ReportOptions actual = parseAddingRequiredArgs("");
    assertFalse(actual.shouldExportLineCoverage());
  }

  @Test
  public void shouldIncludeLaunchClasspathByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs("");
    assertTrue(actual.isIncludeLaunchClasspath());
  }

  @Test
  public void shouldNotIncludeLaunchClasspathWhenFlagUnset() {
    final ReportOptions actual = parseAddingRequiredArgs("--includeLaunchClasspath=false");
    assertFalse(actual.isIncludeLaunchClasspath());
  }

  @Test
  public void shouldIncludeLaunchClasspathWhenFlag() {
    final ReportOptions actual = parseAddingRequiredArgs("--includeLaunchClasspath=true");
    assertTrue(actual.isIncludeLaunchClasspath());
  }

  @Test
  public void shouldHandleNotCanonicalLaunchClasspathElements() {
    final String oldClasspath = System.getProperty(JAVA_CLASS_PATH_PROPERTY);
    try {
      // given
      final PluginServices plugins = PluginServices.makeForContextLoader();
      this.testee = new OptionsParser(new PluginFilter(plugins));
      // and
      System.setProperty(JAVA_CLASS_PATH_PROPERTY,
          getNonCanonicalGregorEngineClassPath());
      // when
      final ReportOptions actual = parseAddingRequiredArgs("--includeLaunchClasspath=false");
      // then
      assertThat(actual.getClassPath().findClasses(gregorClass())).hasSize(1);
    } finally {
      System.setProperty(JAVA_CLASS_PATH_PROPERTY, oldClasspath);
    }
  }

  @Test
  public void shouldCreateEmptyPluginPropertiesWhenNoneSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("");
    assertNotNull(actual.getFreeFormProperties());
  }

  @Test
  public void shouldIncludePluginPropertyValuesWhenSingleKey() {
    final ReportOptions actual = parseAddingRequiredArgs("-pluginConfiguration=foo=1");
    assertEquals("1", actual.getFreeFormProperties().getProperty("foo"));
  }

  @Test
  public void shouldIncludePluginPropertyValuesWhenMultipleKeys() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "-pluginConfiguration=foo=1", "-pluginConfiguration=bar=2");
    assertEquals("1", actual.getFreeFormProperties().getProperty("foo"));
    assertEquals("2", actual.getFreeFormProperties().getProperty("bar"));
  }

  private String getNonCanonicalGregorEngineClassPath() {
    final String gregorEngineClassPath = GregorMutationEngine.class
        .getProtectionDomain().getCodeSource().getLocation().getFile();
    final int lastOccurrenceOfFileSeparator = gregorEngineClassPath
        .lastIndexOf(JAVA_PATH_SEPARATOR);
    return new StringBuilder(gregorEngineClassPath).replace(
        lastOccurrenceOfFileSeparator, lastOccurrenceOfFileSeparator + 1,
        JAVA_PATH_SEPARATOR + "." + JAVA_PATH_SEPARATOR).toString();
  }

  private Predicate<String> gregorClass() {
    return new Predicate<String>() {
      @Override
      public Boolean apply(String s) {
        return GregorMutationEngine.class.getName().equals(s);
      }
    };
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
