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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.config.ConfigOption;
import org.pitest.mutationtest.config.ExecutionMode;
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
    assertThat(actual.getReportDir()).isEqualTo(value);
  }

  @Test
  public void shouldConfigReportDir() {
    final String value = "foo";
    final ReportOptions actual = parseAddingRequiredArgs("--configDir", value);
    assertThat(actual.getConfigDir()).isEqualTo(value);
  }


  @Test
  public void shouldCreatePredicateFromCommaSeparatedListOfTargetClassGlobs() {
    final ReportOptions actual = parseAddingRequiredArgs("--targetClasses",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetClassesFilter();
    assertThat(actualPredicate.test("foo_anything")).isTrue();
    assertThat(actualPredicate.test("bar_anything")).isTrue();
    assertThat(actualPredicate.test("notfoobar")).isFalse();
  }

  @Test
  public void shouldParseCommaSeparatedListOfSourceDirectories() {
    final ReportOptions actual = parseAddingRequiredArgs("--sourceDirs",
        "foo/bar,bar/far");
    assertThat(actual.getSourcePaths()).containsExactly(Path.of("foo/bar"), Path.of(("bar/far")));
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
    assertThat(actual.getJvmArgs()).containsExactlyElementsOf(expected);
  }

  @Test
  public void shouldParseCommaSeparatedListOfMutationOperators() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutators",
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY.name() + ","
            + MathMutator.MATH.name());
    assertThat(actual.getMutators()).containsExactlyElementsOf(Arrays.asList(
        ConditionalsBoundaryMutator.CONDITIONALS_BOUNDARY.name(),
        MathMutator.MATH.name()));
  }

  @Test
  public void shouldParseCommaSeparatedListOfFeatures() {
    final ReportOptions actual = parseAddingRequiredArgs("--features", "+FOO(),-BAR(value=1 & value=2)");
    assertThat(actual.getFeatures()).contains("+FOO()", "-BAR(value=1 & value=2)");
  }

  @Test
  public void shouldDetectInlinedCodeByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.isDetectInlinedCode()).isTrue();
  }

  @Test
  public void shouldDetermineIfInlinedCodeFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--detectInlinedCode");
    assertThat(actual.isDetectInlinedCode()).isTrue();
  }

  @Test
  public void shouldDetermineIfInlinedCodeFlagIsSetWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--detectInlinedCode=true");
    assertThat(actual.isDetectInlinedCode()).isTrue();
  }

  @Test
  public void shouldDetermineIfInlinedCodeFlagIsSetWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--detectInlinedCode=false");
    assertThat(actual.isDetectInlinedCode()).isFalse();
  }

  @Test
  public void shouldNotCreateTimestampedReportsByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.shouldCreateTimeStampedReports()).isFalse();
  }

  @Test
  public void shouldDetermineIfTimestampedReportsFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--timestampedReports");
    assertThat(actual.shouldCreateTimeStampedReports()).isTrue();
  }

  @Test
  public void shouldDetermineIfTimestampedReportsFlagIsSetWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--timestampedReports=true");
    assertThat(actual.shouldCreateTimeStampedReports()).isTrue();
  }

  @Test
  public void shouldDetermineIfTimestampedReportsFlagIsSetWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--timestampedReports=false");
    assertThat(actual.shouldCreateTimeStampedReports()).isFalse();
  }

  @Test
  public void shouldParseNumberOfThreads() {
    final ReportOptions actual = parseAddingRequiredArgs("--threads", "42");
    assertThat(actual.getNumberOfThreads()).isEqualTo(42);
  }

  @Test
  public void shouldParseTimeOutFactor() {
    final ReportOptions actual = parseAddingRequiredArgs("--timeoutFactor",
        "1.32");
    assertThat(actual.getTimeoutFactor()).isEqualTo(1.32f);
  }

  @Test
  public void shouldParseTimeOutConstant() {
    final ReportOptions actual = parseAddingRequiredArgs("--timeoutConst", "42");
    assertThat(actual.getTimeoutConstant()).isEqualTo(42);
  }

  @Test
  public void shouldParseCommaSeparatedListOfTargetTestClassGlobs() {
    final ReportOptions actual = parseAddingRequiredArgs("--targetTest",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertThat(actualPredicate.test("foo_anything")).isTrue();
    assertThat(actualPredicate.test("bar_anything")).isTrue();
    assertThat(actualPredicate.test("notfoobar")).isFalse();
  }

  @Test
  public void shouldParseCommaSeparatedListOfTargetTestClassGlobAsRegex() {
    ReportOptions actual = parseAddingRequiredArgs("--targetTest",
            "~foo\\w*,~bar.*");
    Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertThat(actualPredicate.test("foo_anything")).isTrue();
    assertThat(actualPredicate.test("bar_anything")).isTrue();
    assertThat(actualPredicate.test("notfoobar")).isFalse();
    actual = parseAddingRequiredArgs("--targetTest",
            "~.*?foo\\w*,~bar.*");
    actualPredicate = actual.getTargetTestsFilter();
    assertThat(actualPredicate.test("notfoobar")).isTrue();
  }

  @Test
  public void shouldUseTargetClassesFilterForTestsWhenNoTargetTestsFilterSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--targetClasses",
        "foo*,bar*");
    final Predicate<String> actualPredicate = actual.getTargetTestsFilter();
    assertThat(actualPredicate.test("foo_anything")).isTrue();
    assertThat(actualPredicate.test("bar_anything")).isTrue();
    assertThat(actualPredicate.test("notfoobar")).isFalse();
  }

  @Test
  public void shouldParseCommaSeparatedListOfExcludedTestClassGlobs() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedTestClasses",
        "foo*", "--targetTests", "foo*,bar*", "--targetClasses", "foo*,bar*");
    final Predicate<String> testPredicate = actual.getTargetTestsFilter();
    assertThat(testPredicate.test("foo_anything")).isFalse();
    assertThat(testPredicate.test("bar_anything")).isTrue();
  }

  @Test
  public void shouldParseCommaSeparatedListOfExcludedClassGlobsAndApplyTheseToTargets() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedClasses",
        "foo*", "--targetTests", "foo*,bar*", "--targetClasses", "foo*,bar*");

    final Predicate<String> targetPredicate = actual.getTargetClassesFilter();
    assertThat(targetPredicate.test("foo_anything")).isFalse();
    assertThat(targetPredicate.test("bar_anything")).isTrue();
  }

  @Test
  public void shouldDefaultLoggingPackagesToDefaultsDefinedByDefaultMutationConfigFactory() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.getLoggingClasses()).containsExactlyElementsOf(ReportOptions.LOGGING_CLASSES);
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
    assertThat(actual.getLoggingClasses()).containsExactly("foo", "bar", "foo.bar");
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
    assertThat(actual.getOutputFormats()).containsExactly("HTML");
  }

  @Test
  public void shouldParseCommaSeparatedListOfOutputFormatsWhenSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--outputFormats",
        "HTML,CSV");
    assertThat(actual.getOutputFormats()).containsExactlyInAnyOrder("HTML", "CSV");
  }

  @Test
  public void shouldAcceptCommaSeparatedListOfAdditionalClassPathElements() {
    final ReportOptions ro = parseAddingRequiredArgs("--classPath",
        "/foo/bar,./boo");
    final Collection<String> actual = ro.getClassPathElements();
    assertThat(actual).contains("/foo/bar");
    assertThat(actual).contains("./boo");
  }

  @Test
  public void shouldAcceptFileWithListOfAdditionalClassPathElements() {
    final ClassLoader classLoader = getClass().getClassLoader();
    final File classPathFile = new File(classLoader.getResource("testClassPathFile.txt").getFile());
    final ReportOptions ro = parseAddingRequiredArgs("--classPathFile",
	    classPathFile.getAbsolutePath());
    final Collection<String> actual = ro.getClassPathElements();
    assertThat(actual).contains("C:/foo");
    assertThat(actual).contains("/etc/bar");
  }


  @Test
  public void shouldFailWhenNoMutationsSetByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.shouldFailWhenNoMutations()).isTrue();
  }

  @Test
  public void shouldFailWhenNoMutationsWhenFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--failWhenNoMutations");
    assertThat(actual.shouldFailWhenNoMutations()).isTrue();
  }

  @Test
  public void shouldFailWhenNoMutationsWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--failWhenNoMutations=true");
    assertThat(actual.shouldFailWhenNoMutations()).isTrue();
  }

  @Test
  public void shouldNotFailWhenNoMutationsWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--failWhenNoMutations=false");
    assertThat(actual.shouldFailWhenNoMutations()).isFalse();
  }

  @Test
  public void shouldNotSkipFailingTestsByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.skipFailingTests()).isFalse();
  }

  @Test
  public void shouldSkipFailingTestsWhenFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--skipFailingTests");
    assertThat(actual.skipFailingTests()).isTrue();
  }

  @Test
  public void shouldSkipFailingTestsWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--skipFailingTests=true");
    assertThat(actual.skipFailingTests()).isTrue();
  }

  @Test
  public void shouldNotSkipFailingTestsWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--skipFailingTests=false");
    assertThat(actual.skipFailingTests()).isFalse();
  }

  @Test
  public void shouldParseCommaSeparatedListOfMutableCodePaths() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutableCodePaths",
        "foo,bar");
    assertThat(actual.getCodePaths()).containsExactly("foo", "bar");
  }

  @Test
  public void shouldParseCommaSeparatedListOfExcludedTestGroups() {
    final ReportOptions actual = parseAddingRequiredArgs("--excludedGroups",
        "foo,bar");
    assertThat(actual.getGroupConfig().getExcludedGroups()).containsExactly("foo", "bar");
  }

  @Test
  public void shouldParseCommaSeparatedListOfIncludedTestGroups() {
    final ReportOptions actual = parseAddingRequiredArgs("--includedGroups",
        "foo,bar");
    assertThat(actual.getGroupConfig().getIncludedGroups()).containsExactly("foo", "bar");
  }

  @Test
  public void shouldParseCommaSeparatedListOfIncludedTestMethods() {
    final ReportOptions actual = parseAddingRequiredArgs("--includedTestMethods",
            "foo,bar");
    assertThat(actual.getIncludedTestMethods()).containsExactly("foo", "bar");
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
    assertThat(actual.getMutationUnitSize()).isEqualTo(50);
  }

  @Test
  public void shouldDefaultMutationUnitSizeToCorrectValue() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.getMutationUnitSize()).isEqualTo(
        (int) ConfigOption.MUTATION_UNIT_SIZE.getDefault(Integer.class));
  }

  @Test
  public void shouldDefaultToNoHistory() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.getHistoryInputLocation()).isNull();
    assertThat(actual.getHistoryOutputLocation()).isNull();
  }

  @Test
  public void shouldParseHistoryInputLocation() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--historyInputLocation", "foo");
    assertThat(actual.getHistoryInputLocation()).isEqualTo(new File("foo"));
  }

  @Test
  public void shouldParseHistoryOutputLocation() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "--historyOutputLocation", "foo");
    assertThat(actual.getHistoryOutputLocation()).isEqualTo(new File("foo"));
  }

  @Test
  public void shouldParseMutationThreshold() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutationThreshold",
        "42");
    assertThat(actual.getMutationThreshold()).isEqualTo(42);
  }

  @Test
  public void shouldParseTestStrengthThreshold() {
    final ReportOptions actual = parseAddingRequiredArgs("--testStrengthThreshold",
            "50");
    assertThat(actual.getTestStrengthThreshold()).isEqualTo(50);
  }

  @Test
  public void shouldParseMaximumAllowedSurvivingMutants() {
    final ReportOptions actual = parseAddingRequiredArgs("--maxSurviving",
        "42");
    assertThat(actual.getMaximumAllowedSurvivors()).isEqualTo(42);
  }

  @Test
  public void shouldParseCoverageThreshold() {
    final ReportOptions actual = parseAddingRequiredArgs("--coverageThreshold",
        "42");
    assertThat(actual.getCoverageThreshold()).isEqualTo(42);
  }

  @Test
  public void shouldDefaultToGregorEngineWhenNoOptionSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.getMutationEngine()).isEqualTo("gregor");
  }

  @Test
  public void shouldParseMutationEnigne() {
    final ReportOptions actual = parseAddingRequiredArgs("--mutationEngine",
        "foo");
    assertThat(actual.getMutationEngine()).isEqualTo("foo");
  }

  @Test
  public void shouldDefaultJVMToNull() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.getJavaExecutable()).isNull();
  }

  @Test
  public void shouldParseJVM() {
    final ReportOptions actual = parseAddingRequiredArgs("--jvmPath", "foo");
    assertThat(actual.getJavaExecutable()).isEqualTo("foo");
  }

  @Test
  public void shouldNotExportLineCoverageByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.shouldExportLineCoverage()).isFalse();
  }

  @Test
  public void shouldDetermineIfExportLineCoverageFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--exportLineCoverage");
    assertThat(actual.shouldExportLineCoverage()).isTrue();
  }

  @Test
  public void shouldDetermineIfExportLineCoverageFlagIsSetWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--exportLineCoverage=true");
    assertThat(actual.shouldExportLineCoverage()).isTrue();
  }

  @Test
  public void shouldDetermineIfExportLineCoverageFlagIsSetWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--exportLineCoverage=false");
    assertThat(actual.shouldExportLineCoverage()).isFalse();
  }

  @Test
  public void shouldIncludeLaunchClasspathByDefault() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.isIncludeLaunchClasspath()).isTrue();
  }

  @Test
  public void shouldIncludeLaunchClasspathWhenFlag() {
    final ReportOptions actual = parseAddingRequiredArgs("--includeLaunchClasspath");
    assertThat(actual.isIncludeLaunchClasspath()).isTrue();
  }

  @Test
  public void shouldIncludeLaunchClasspathWhenFlagTrue() {
    final ReportOptions actual = parseAddingRequiredArgs("--includeLaunchClasspath=true");
    assertThat(actual.isIncludeLaunchClasspath()).isTrue();
  }

  @Test
  public void shouldNotIncludeLaunchClasspathWhenFlagFalse() {
    final ReportOptions actual = parseAddingRequiredArgs("--includeLaunchClasspath=false");
    assertThat(actual.isIncludeLaunchClasspath()).isFalse();
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
    assertThat(actual.getFreeFormProperties()).isNotNull();
  }

  @Test
  public void shouldIncludePluginPropertyValuesWhenSingleKey() {
    final ReportOptions actual = parseAddingRequiredArgs("-pluginConfiguration=foo=1");
    assertThat(actual.getFreeFormProperties().getProperty("foo")).isEqualTo("1");
  }

  @Test
  public void shouldIncludePluginPropertyValuesWhenMultipleKeys() {
    final ReportOptions actual = parseAddingRequiredArgs(
        "-pluginConfiguration=foo=1", "-pluginConfiguration=bar=2");
    assertThat(actual.getFreeFormProperties().getProperty("foo")).isEqualTo("1");
    assertThat(actual.getFreeFormProperties().getProperty("bar")).isEqualTo("2");
  }


  @Test
  public void shouldNotErrorWhenLegacyClasspathJarWhenFlagSet() {
    assertThatCode(() -> parseAddingRequiredArgs("--useClasspathJar")).doesNotThrowAnyException();
  }

  @Test
  public void shouldDefaultMatrixFlagToFalse() {
    final ReportOptions actual = parseAddingRequiredArgs();
    assertThat(actual.isFullMutationMatrix()).isFalse();
  }

  @Test
  public void shouldMatrixFlagWhenFlagIsSet() {
    final ReportOptions actual = parseAddingRequiredArgs("--fullMutationMatrix");
    assertThat(actual.isFullMutationMatrix()).isTrue();
  }

  @Test
  public void shouldMatrixFlagWhenTrueSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--fullMutationMatrix=true");
    assertThat(actual.isFullMutationMatrix()).isTrue();
  }

  @Test
  public void shouldNotMatrixFlagWhenFalseSupplied() {
    final ReportOptions actual = parseAddingRequiredArgs("--fullMutationMatrix=false");
    assertThat(actual.isFullMutationMatrix()).isFalse();
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

  @Test
  public void defaultsToNormalExecution() {
    final ReportOptions actual = parseAddingRequiredArgs("");
    assertThat(actual.mode()).isEqualTo(ExecutionMode.NORMAL);
  }

  @Test
  public void parsesShortFormDryRun() {
    final ReportOptions actual = parseAddingRequiredArgs(
            "--dryRun");
    assertThat(actual.mode()).isEqualTo(ExecutionMode.DRY_RUN);
  }

  @Test
  public void parsesLongFormDryRunFalse() {
    ReportOptions actual = parseAddingRequiredArgs(
            "--dryRun=false");
    assertThat(actual.mode()).isEqualTo(ExecutionMode.NORMAL);
  }

  @Test
  public void parsesLongFormDryRunTrue() {
    ReportOptions actual = parseAddingRequiredArgs(
            "--dryRun=true");
    assertThat(actual.mode()).isEqualTo(ExecutionMode.DRY_RUN);
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

    final List<String> a = new ArrayList<>(Arrays.asList(args));
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
