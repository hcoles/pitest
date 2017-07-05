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
package org.pitest.maven;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.model.Plugin;
import org.codehaus.plexus.util.xml.Xpp3Dom;
import org.mockito.Mockito;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.config.ConfigOption;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.util.Unchecked;

public class MojoToReportOptionsConverterTest extends BasePitMojoTest {

  private MojoToReportOptionsConverter testee;
  private SurefireConfigConverter      surefireConverter;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    Plugin surefire = new Plugin();
    surefire.setGroupId("org.apache.maven.plugins");
    surefire.setArtifactId("maven-surefire-plugin");
    this.surefireConverter = Mockito.mock(SurefireConfigConverter.class);
    List<Plugin> mavenPlugins = Collections.singletonList(surefire);
    when(this.project.getBuildPlugins()).thenReturn(mavenPlugins);
    Build build = new Build();
    build.setOutputDirectory("");
    when(this.project.getBuild()).thenReturn(build);
  }

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

  public void testUsesSourceDirectoriesFromProject() {
    when(this.project.getCompileSourceRoots()).thenReturn(Arrays.asList("src"));
    when(this.project.getTestCompileSourceRoots()).thenReturn(
        Arrays.asList("tst"));
    final ReportOptions actual = parseConfig("");
    assertEquals(Arrays.asList(new File("src"), new File("tst")),
        actual.getSourceDirs());
  }

  public void testParsesMaxDependencyDistance() {
    final ReportOptions actual = parseConfig("<maxDependencyDistance>42</maxDependencyDistance>");
    assertEquals(42, actual.getDependencyAnalysisMaxDistance());
  }
  
  public void testParsesExcludedRunners() {
    String runner = "org.springframework.test.context.junit4.SpringJUnit4ClassRunner";
    final ReportOptions actual = parseConfig("<excludedRunners><param>" + runner + "</param></excludedRunners>");
    assertThat(actual.getExcludedRunners()).hasSize(1).containsExactly(runner);
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
        "                      <param>foo</param>" + //
        "                      <param>bar</param>" + //
        "                  </mutators>";
    final ReportOptions actual = parseConfig(xml);
    assertEquals(Arrays.asList("foo", "bar"), actual.getMutators());
  }
  
  public void testParsesListOfFeatures() {
    final String xml = "<features>" + //
        "                      <param>+FOO</param>" + //
        "                      <param>-BAR(foo[1] bar[3])</param>" + //
        "               </features>";
    final ReportOptions actual = parseConfig(xml);
    assertThat(actual.getFeatures()).contains("+FOO", "-BAR(foo[1] bar[3])");
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
    assertEquals(ReportOptions.LOGGING_CLASSES, actual.getLoggingClasses());
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

  public void testParsesDetectInlineCodeFlag() {
    assertTrue(parseConfig("<detectInlinedCode>true</detectInlinedCode>")
        .isDetectInlinedCode());
    assertFalse(parseConfig("<detectInlinedCode>false</detectInlinedCode>")
        .isDetectInlinedCode());
  }

  public void testDefaultsToHtmlReportWhenNoOutputFormatsSpecified() {
    final ReportOptions actual = parseConfig("");
    assertEquals(new HashSet<String>(Arrays.asList("HTML")),
        actual.getOutputFormats());
  }

  public void testParsesListOfOutputFormatsWhenSupplied() {
    final String xml = "<outputFormats>" + //
        "                      <param>HTML</param>" + //
        "                      <param>CSV</param>" + //
        "                  </outputFormats>";
    final ReportOptions actual = parseConfig(xml);
    assertEquals(new HashSet<String>(Arrays.asList("HTML", "CSV")),
        actual.getOutputFormats());
  }

  public void testObeysFailWhenNoMutationsFlagWhenPackagingTypeIsNotPOM() {
    when(this.project.getModel()).thenReturn(new Model());
    assertTrue(parseConfig("<failWhenNoMutations>true</failWhenNoMutations>")
        .shouldFailWhenNoMutations());
    assertFalse(parseConfig("<failWhenNoMutations>false</failWhenNoMutations>")
        .shouldFailWhenNoMutations());
  }

  public void testParsesTestGroupsToExclude() {
    final ReportOptions actual = parseConfig("<excludedGroups><value>foo</value><value>bar</value></excludedGroups>");
    assertEquals(Arrays.asList("foo", "bar"), actual.getGroupConfig()
        .getExcludedGroups());
  }

  public void testParsesTestGroupsToInclude() {
    final ReportOptions actual = parseConfig("<includedGroups><value>foo</value><value>bar</value></includedGroups>");
    assertEquals(Arrays.asList("foo", "bar"), actual.getGroupConfig()
        .getIncludedGroups());
  }

  public void testMaintainsOrderOfClassPath() {
    final ReportOptions actual = parseConfig("<includedGroups><value>foo</value><value>bar</value></includedGroups>");
    assertEquals(this.classPath, actual.getClassPathElements());
  }

  public void testParsesMutationUnitSize() {
    final ReportOptions actual = parseConfig("<mutationUnitSize>50</mutationUnitSize>");
    assertEquals(50, actual.getMutationUnitSize());
  }

  public void testDefaultsMutationUnitSizeToCorrectValue() {
    final ReportOptions actual = parseConfig("");
    assertEquals(
        (int) ConfigOption.MUTATION_UNIT_SIZE.getDefault(Integer.class),
        actual.getMutationUnitSize());
  }

  public void testParsesTimeStampedReports() {
    final ReportOptions actual = parseConfig("<timestampedReports>false</timestampedReports>");
    assertEquals(false, actual.shouldCreateTimeStampedReports());
  }

  public void testParsesHistoryInputFile() {
    final ReportOptions actual = parseConfig("<historyInputFile>foo</historyInputFile>");
    assertEquals(new File("foo"), actual.getHistoryInputLocation());
  }

  public void testParsesHistoryOutputFile() {
    final ReportOptions actual = parseConfig("<historyOutputFile>foo</historyOutputFile>");
    assertEquals(new File("foo"), actual.getHistoryOutputLocation());
  }
  
  public void testParsesLocalHistoryFlag() {
    when(this.project.getGroupId()).thenReturn("com.example");
    when(this.project.getArtifactId()).thenReturn("foo");    
    when(this.project.getVersion()).thenReturn("0.1-SNAPSHOT");      
    final ReportOptions actual = parseConfig("<withHistory>true</withHistory>");
    String expected = "com.example.foo.0.1-SNAPSHOT_pitest_history.bin";
    assertThat(actual.getHistoryInputLocation().getAbsolutePath()).endsWith(expected);
  }

  public void testParsesLineCoverageExportFlagWhenSet() {
    final ReportOptions actual = parseConfig("<exportLineCoverage>true</exportLineCoverage>");
    assertTrue(actual.shouldExportLineCoverage());
  }

  public void testParsesLineCoverageExportFlagWhenNotSet() {
    final ReportOptions actual = parseConfig("<exportLineCoverage>false</exportLineCoverage>");
    assertFalse(actual.shouldExportLineCoverage());
  }

  public void testParsesEngineWhenSet() {
    final ReportOptions actual = parseConfig("<mutationEngine>foo</mutationEngine>");
    assertEquals("foo", actual.getMutationEngine());
  }

  public void testDefaultsJavaExecutableToNull() {
    final ReportOptions actual = parseConfig("");
    assertEquals(null, actual.getJavaExecutable());
  }

  public void testParsesJavaExecutable() {
    final ReportOptions actual = parseConfig("<jvm>foo</jvm>");
    assertEquals("foo", actual.getJavaExecutable());
  }

  public void testParsesExcludedClasspathElements()
      throws DependencyResolutionRequiredException {
    final String sep = File.pathSeparator;

    final Set<Artifact> artifacts = new HashSet<Artifact>();
    final Artifact dependency = Mockito.mock(Artifact.class);
    when(dependency.getGroupId()).thenReturn("group");
    when(dependency.getArtifactId()).thenReturn("artifact");
    when(dependency.getFile()).thenReturn(
        new File("group" + sep + "artifact" + sep + "1.0.0" + sep
            + "group-artifact-1.0.0.jar"));
    artifacts.add(dependency);
    when(this.project.getArtifacts()).thenReturn(artifacts);
    when(this.project.getTestClasspathElements()).thenReturn(
        Arrays.asList("group" + sep + "artifact" + sep + "1.0.0" + sep
            + "group-artifact-1.0.0.jar"));

    final ReportOptions actual = parseConfig("<classpathDependencyExcludes>"
        + "										<param>group:artifact</param>"
        + "									</classpathDependencyExcludes>");
    assertFalse(actual.getClassPathElements().contains(
        "group" + sep + "artifact" + sep + "1.0.0" + sep
        + "group-artifact-1.0.0.jar"));
  }

  public void testParsesSurefireConfigWhenFlagSet() {
    parseConfig("<parseSurefireConfig>true</parseSurefireConfig>");
    verify(this.surefireConverter).update(any(ReportOptions.class),
        any(Xpp3Dom.class));
  }

  public void testIgnoreSurefireConfigWhenFlagNotSet() {
    parseConfig("<parseSurefireConfig>false</parseSurefireConfig>");
    verify(this.surefireConverter, never()).update(any(ReportOptions.class),
        any(Xpp3Dom.class));
  }

  public void testParsesCustomProperties() {
    final ReportOptions actual = parseConfig("<pluginConfiguration><foo>foo</foo><bar>bar</bar></pluginConfiguration>");
    assertEquals("foo", actual.getFreeFormProperties().get("foo"));
    assertEquals("bar", actual.getFreeFormProperties().get("bar"));
  }

  private ReportOptions parseConfig(final String xml) {
    try {
      final String pom = createPomWithConfiguration(xml);
      final AbstractPitMojo mojo = createPITMojo(pom);
      @SuppressWarnings("unchecked")
      Predicate<Artifact> filter = Mockito.mock(Predicate.class);
      when(
          this.surefireConverter.update(any(ReportOptions.class),
              any(Xpp3Dom.class))).then(returnsFirstArg());
      this.testee = new MojoToReportOptionsConverter(mojo,
          this.surefireConverter, filter);
      final ReportOptions actual = this.testee.convert();
      return actual;
    } catch (final Exception ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}
