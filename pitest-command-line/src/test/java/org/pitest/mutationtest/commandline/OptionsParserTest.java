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
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.config.ConfigOption;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.engine.gregor.GregorMutationEngine;
import org.pitest.mutationtest.engine.gregor.mutators.ConditionalsBoundaryMutator;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.util.Verbosity;

public class OptionsParserTest {

  private static final String JAVA_PATH_SEPARATOR      = "/";

  private static final String JAVA_CLASS_PATH_PROPERTY = "java.class.path";

  private OptionsParser       testee;

  @Mock
  private Predicate<String>   filter;

  @Before
  public void setUp() {
    MockitoAnnotations.openMocks(this);
    when(this.filter.test(any(String.class))).thenReturn(true);
    this.testee = new OptionsParser(this.filter);
  }

  @Test
  public void shouldParseTestPlugin() {
    final String value = "foo";
    assertThatCode(() -> parseAddingRequiredArgs("--testPlugin", value))
            .doesNotThrowAnyException();
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
    assertTrue(actualPredicate.test("foo_anything"));
    assertTrue(actualPredicate.test("bar_anything"));
    assertFalse(actualPredicate.test("notfoobar"));
  }

  @Test
  public void shouldParseCommaSeparatedListOfSourceDirectories() {
    final ReportOptions actual = parseAddingRequiredArgs("--sourceDirs",
        "foo/bar,bar/far");
    assertThat(actual.getSourcePaths()).containsExactly(Paths.get("foo/bar"), Paths.get(("bar/far")));
  }

  @Test
  public void shouldSetArgLine() {
    final ReportOptions actual = parseAddingRequiredArgs("--argLine", "-Dfoo=\"bar\"");

    assertThat(actual.getArgLine()).isEqualTo("-Dfoo=\"bar\"");
  }

  @Test
  public void shouldParseCommaSeparatedListOfJVMArgs() {
    final ReportOptions actual = parseAddingRequiredArgs("--jvmArgs", "foo,bar");

    List<String> expected = new ArrayList<>();
    expected.add("foo");
    expected.add("bar");
    assertEquals(expected, actual.getJvmArgs());
  }

  @Test
  public void shouldParseCommaSeparatedListOfMutationOperators() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutators",
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY.name() + ","
            + MathMutator.MATH.name());
    assertEquals(Arrays.asList(
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY.name(),
        MathMutator.MATH.name()), actual.getMutators());
  }

  @Test
  public void shouldParseCommaSeparatedListOfFeatures() {
    final ReportOptions actual = parseAddingRequiredArgs("--features", "+FOO(),-BAR(value=1 & value=2)");
    assertThat(actual.getFeatures()).contains("+FOO()", "-BAR(value=1 & value=2)");
  }

  @Test
  public void shouldDetectInlinedCodeByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertTrue(actual.isDetectInlinedCode());
  }

  @Test
  public void shouldDetermineIfInlinedCodeFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--detectInlinedCode");
    assertTrue(actual.isDetectInlinedCode());
  }

  @Test
  public void shouldDetermineIfInlinedCodeFlagIsSetWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--detectInlinedCode=true");
    assertTrue(actual.isDetectInlinedCode());
  }

  @Test
  public void shouldDetermineIfInlinedCodeFlagIsSetWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--detectInlinedCode=false");
    assertFalse(actual.isDetectInlinedCode());
  }

  @Test
  public void shouldNotCreateTimestampedReportsByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertFalse(actual.shouldCreateTimeStampedReports());
  }

  @Test
  public void shouldDetermineIfTimestampedReportsFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--timestampedReports");
    assertTrue(actual.shouldCreateTimeStampedReports());
  }

  @Test
  public void shouldDetermineIfTimestampedReportsFlagIsSetWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--timestampedReports=true");
    assertTrue(actual.shouldCreateTimeStampedReports());
  }

  @Test
  public void shouldDetermineIfTimestampedReportsFlagIsSetWhenFalseSupplied() {
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
    assertTrue(actualPredicate.test("foo_anything"));
    assertTrue(actualPredicate.test("bar_anything"));
    assertFalse(actualPredicate.test("notfoobar"));
  }

  @Test
  public void shouldParseCommaSeparatedListOfTargetTestClassGlobAsRegex() {
    ReportOptions actual = parseAddingRequiredArgs("--targetTest",
            "~foo\\w*,~bar.*");
    Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.test("foo_anything"));
    assertTrue(actualPredicate.test("bar_anything"));
    assertFalse(actualPredicate.test("notfoobar"));
    actual = parseAddingRequiredArgs("--targetTest",
            "~.*?foo\\w*,~bar.*");
    actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.test("notfoobar"));
  }

  @Test
  public void shouldUseTargetClassesFilterForTestsWhenNoTargetTestsFilterSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--targetClasses",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertTrue(actualPredicate.test("foo_anything"));
    assertTrue(actualPredicate.test("bar_anything"));
    assertFalse(actualPredicate.test("notfoobar"));
  }

  @Test
  public void shouldParseCommaSeparatedListOfExcludedTestClassGlobs() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedTestClasses",
        "foo*", "--targetTests", "foo*,bar*", "--targetClasses", "foo*,bar*");
    final Predicate<String> testPredicate = actual.getTargetTestsFilter();
    assertFalse(testPredicate.test("foo_anything"));
    assertTrue(testPredicate.test("bar_anything"));
  }

  @Test
  public void shouldParseCommaSeparatedListOfExcludedClassGlobsAndApplyTheseToTargets() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedClasses",
        "foo*", "--targetTests", "foo*,bar*", "--targetClasses", "foo*,bar*");

    final Predicate<String> targetPredicate = actual.getTargetClassesFilter();
    assertFalse(targetPredicate.test("foo_anything"));
    assertTrue(targetPredicate.test("bar_anything"));
  }

  @Test
  public void shouldDefaultLoggingPackagesToDefaultsDefinedByDefaultMutationConfigFactory() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertEquals(ReportOptions.LOGGING_CLASSES, actual.getLoggingClasses());
  }

  @Test
  public void shouldAvoidJBossLoggingByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.getLoggingClasses()).contains("org.jboss.logging");
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
    final Collection<String> actualPredicate = actual
        .getExcludedMethods();
    assertThat(actualPredicate).containsExactlyInAnyOrder("foo*", "bar*", "car");
  }

  @Test
  public void shouldDefaultToDefaultVerbosity() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.getVerbosity()).isEqualTo(Verbosity.DEFAULT);
  }

  @Test
  public void shouldDetermineIfVerboseFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--verbose");
    assertThat(actual.getVerbosity()).isEqualTo(Verbosity.VERBOSE);
  }

  @Test
  public void shouldDetermineIfVerboseFlagIsSetWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--verbose=true");
    assertThat(actual.getVerbosity()).isEqualTo(Verbosity.VERBOSE);
  }

  @Test
  public void shouldDetermineIfVerboseFlagIsSetWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--verbose=false");
    assertThat(actual.getVerbosity()).isEqualTo(Verbosity.DEFAULT);
  }

  @Test
  public void shouldParseVerbosity() {
    final ReportOptions actual = parseAddingRequiredArgs("--verbosity", "quiet");
    assertThat(actual.getVerbosity()).isEqualTo(Verbosity.QUIET);
  }

  @Test
  public void positiveVerboseFlagOverridesVerbosity() {
    final ReportOptions actual = parseAddingRequiredArgs("--verbose", "--verbosity", "quiet");
    assertThat(actual.getVerbosity()).isEqualTo(Verbosity.VERBOSE);
  }

  @Test
  public void shouldDefaultToHtmlReportWhenNoOutputFormatsSpecified() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertEquals(new HashSet<>(Arrays.asList("HTML")),
        actual.getOutputFormats());
  }

  @Test
  public void shouldParseCommaSeparatedListOfOutputFormatsWhenSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--outputFormats",
        "HTML,CSV");
    assertEquals(new HashSet<>(Arrays.asList("HTML", "CSV")),
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
    final ClassLoader classLoader = getClass().getClassLoader();
    final File classPathFile = new File(classLoader.getResource("testClassPathFile.txt").getFile());
    final ReportOptions ro = parseAddingRequiredArgs("--classPathFile",
	    classPathFile.getAbsolutePath());
    final Collection<String> actual = ro.getClassPathElements();
    assertTrue(actual.contains("C:/foo"));
    assertTrue(actual.contains("/etc/bar"));
  }

  @Test
  public void alsoSetsUseClasspathJarWhenClasspathFileProvided() {
    final ClassLoader classLoader = getClass().getClassLoader();
    final File classPathFile = new File(classLoader.getResource("testClassPathFile.txt").getFile());
    final ReportOptions ro = parseAddingRequiredArgs("--classPathFile",
            classPathFile.getAbsolutePath());
    assertThat(ro.useClasspathJar()).isTrue();
  }

  @Test
  public void shouldFailWhenNoMutationsSetByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertTrue(actual.shouldFailWhenNoMutations());
  }

  @Test
  public void shouldFailWhenNoMutationsWhenFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--failWhenNoMutations");
    assertTrue(actual.shouldFailWhenNoMutations());
  }

  @Test
  public void shouldFailWhenNoMutationsWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--failWhenNoMutations=true");
    assertTrue(actual.shouldFailWhenNoMutations());
  }

  @Test
  public void shouldNotFailWhenNoMutationsWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--failWhenNoMutations=false");
    assertFalse(actual.shouldFailWhenNoMutations());
  }

  @Test
  public void shouldNotSkipFailingTestsByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertFalse(actual.skipFailingTests());
  }

  @Test
  public void shouldSkipFailingTestsWhenFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--skipFailingTests");
    assertTrue(actual.skipFailingTests());
  }

  @Test
  public void shouldSkipFailingTestsWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--skipFailingTests=true");
    assertTrue(actual.skipFailingTests());
  }

  @Test
  public void shouldNotSkipFailingTestsWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--skipFailingTests=false");
    assertFalse(actual.skipFailingTests());
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
  public void shouldParseCommaSeparatedListOfIncludedTestMethods() {
    final ReportOptions actual = parseAddingRequiredArgs("--includedTestMethods",
            "foo,bar");
    assertEquals(Arrays.asList("foo", "bar"), actual
        .getIncludedTestMethods());
  }

  @Test
  public void shouldParseCommaSeparatedListOfExcludedTestRunners() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedRunners",
            "foo,bar");
    assertThat(actual.getExcludedRunners()).containsExactly("foo", "bar");
  }

  @Test
  public void shouldParseMutationUnitSize() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutationUnitSize",
        "50");
    assertEquals(50, actual.getMutationUnitSize());
  }

  @Test
  public void shouldDefaultMutationUnitSizeToCorrectValue() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertEquals(
        (int) ConfigOption.MUTATION_UNIT_SIZE.getDefault(Integer.class),
        actual.getMutationUnitSize());
  }

  @Test
  public void shouldDefaultToNoHistory() {
    final ReportOptions actual = parseAddingRequiredArgs();
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
  public void shouldParseTestStrengthThreshold() {
    final ReportOptions actual = parseAddingRequiredArgs("--testStrengthThreshold",
            "50");
    assertEquals(50, actual.getTestStrengthThreshold());
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
  public void shouldNotExportLineCoverageByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertFalse(actual.shouldExportLineCoverage());
  }

  @Test
  public void shouldDetermineIfExportLineCoverageFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--exportLineCoverage");
    assertTrue(actual.shouldExportLineCoverage());
  }

  @Test
  public void shouldDetermineIfExportLineCoverageFlagIsSetWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--exportLineCoverage=true");
    assertTrue(actual.shouldExportLineCoverage());
  }

  @Test
  public void shouldDetermineIfExportLineCoverageFlagIsSetWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--exportLineCoverage=false");
    assertFalse(actual.shouldExportLineCoverage());
  }

  @Test
  public void shouldIncludeLaunchClasspathByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertTrue(actual.isIncludeLaunchClasspath());
  }

  @Test
  public void shouldIncludeLaunchClasspathWhenFlag() {
    final ReportOptions actual = parseAddingRequiredArgs("--includeLaunchClasspath");
    assertTrue(actual.isIncludeLaunchClasspath());
  }

  @Test
  public void shouldIncludeLaunchClasspathWhenFlagTrue() {
    final ReportOptions actual = parseAddingRequiredArgs("--includeLaunchClasspath=true");
    assertTrue(actual.isIncludeLaunchClasspath());
  }

  @Test
  public void shouldNotIncludeLaunchClasspathWhenFlagFalse() {
    final ReportOptions actual = parseAddingRequiredArgs("--includeLaunchClasspath=false");
    assertFalse(actual.isIncludeLaunchClasspath());
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
    final ReportOptions actual = parseAddingRequiredArgs();
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

  @Test
  public void shouldDefaultToNotUsingAClasspathJar() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertFalse(actual.useClasspathJar());
  }

  @Test
  public void shouldUseClasspathJarWhenFlagSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--useClasspathJar");
    assertTrue(actual.useClasspathJar());
  }

  @Test
  public void shouldUseClasspathJarWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--useClasspathJar=true");
    assertTrue(actual.useClasspathJar());
  }

  @Test
  public void shouldNotUseClasspathJarWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--useClasspathJar=false");
    assertFalse(actual.useClasspathJar());
  }

  @Test
  public void shouldDefaultMatrixFlagToFalse() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertFalse(actual.isFullMutationMatrix());
  }

  @Test
  public void shouldMatrixFlagWhenFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--fullMutationMatrix");
    assertTrue(actual.isFullMutationMatrix());
  }

  @Test
  public void shouldMatrixFlagWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--fullMutationMatrix=true");
    assertTrue(actual.isFullMutationMatrix());
  }

  @Test
  public void shouldNotMatrixFlagWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--fullMutationMatrix=false");
    assertFalse(actual.isFullMutationMatrix());
  }

  @Test
  public void shouldParseProjectBase() {
    final ReportOptions actual = parseAddingRequiredArgs(
            "--projectBase", "foo");
    assertThat(actual.getProjectBase()).hasFileName("foo");
  }

  @Test
  public void inputEncodingDefaultsToSystemDefault() {
    final ReportOptions actual = parseAddingRequiredArgs(
            "");
    assertThat(actual.getInputEncoding()).isEqualTo(Charset.defaultCharset());
  }

  @Test
  public void parsesInputEncoding() {
    final ReportOptions actual = parseAddingRequiredArgs(
            "--inputEncoding", "US-ASCII");
    assertThat(actual.getInputEncoding()).isEqualTo(StandardCharsets.US_ASCII);
  }

  @Test
  public void outputEncodingDefaultsToSystemDefault() {
    final ReportOptions actual = parseAddingRequiredArgs(
            "");
    assertThat(actual.getOutputEncoding()).isEqualTo(Charset.defaultCharset());
  }

  @Test
  public void parsesOutputEncoding() {
    final ReportOptions actual = parseAddingRequiredArgs(
            "--outputEncoding", "US-ASCII");
    assertThat(actual.getOutputEncoding()).isEqualTo(StandardCharsets.US_ASCII);
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
    return s -> GregorMutationEngine.class.getName().equals(s);
  }

  private ReportOptions parseAddingRequiredArgs(final String... args) {

    final List<String> a = new ArrayList<>();
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
