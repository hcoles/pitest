package org.pitest.mutationtest.config;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import org.pitest.coverage.CoverageExporter;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.export.DefaultCoverageExporter;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.mutationtest.incremental.DefaultHistoryFactory;
import org.pitest.plugin.Feature;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.PitError;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Collections;
import java.util.function.Consumer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class SettingsFactoryTest {

  private final ReportOptions  options = new ReportOptions();

  private final PluginServices plugins = PluginServices.makeForContextLoader();

  private SettingsFactory      testee;

  @Rule
  public TemporaryFolder reportDir = new TemporaryFolder();

  @Before
  public void setUp() {
    this.testee = new SettingsFactory(this.options, this.plugins);
    this.options.setGroupConfig(new TestGroupConfig());
  }

  @Test
  public void shouldReturnANullCoverageExporterWhenOptionSetToFalse() {
    this.options.setExportLineCoverage(false);
    assertTrue(this.testee.createCoverageExporter() instanceof NullCoverageExporter);
  }

  @Test
  public void usesDefaultCoverageExporterWhenOptionSetToTrueAndNoOtherExportersPresent() throws IOException {
    this.options.setExportLineCoverage(true);
    this.options.setShouldCreateTimestampedReports(false);
    this.options.setReportDir(reportDir.getRoot().getAbsolutePath());
    CoverageExporter actual = this.testee.createCoverageExporter();
    actual.recordCoverage(Collections.emptyList());

    assertThat(Files.list(reportDir.getRoot().toPath()))
            .anyMatch(path -> path.getFileName().toString().equals("linecoverage.xml"));
  }

  @Test
  public void generatesNoCoverageWhenAllExportersDisabled() throws IOException {
    this.options.setExportLineCoverage(true);
    this.options.setShouldCreateTimestampedReports(false);
    this.options.setReportDir(reportDir.getRoot().getAbsolutePath());
    this.options.setFeatures(Arrays.asList("-defaultCoverage"));
    CoverageExporter actual = this.testee.createCoverageExporter();
    actual.recordCoverage(Collections.emptyList());

    assertThat(Files.list(reportDir.getRoot().toPath()))
            .noneMatch(path -> path.getFileName().toString().equals("linecoverage.xml"));
  }

  @Test
  public void shouldReturnEngineWhenRequestedEngineIsKnown() {
    assertTrue(this.testee.createEngine() instanceof GregorEngineFactory);
  }

  @Test(expected = PitError.class)
  public void shouldThrowErrorWhenRequestedEngineNotKnown() {
    this.options.setMutationEngine("unknown");
    this.testee.createEngine();
  }

  @Test
  public void shouldReturnListenerWhenRequestedListenerIsKnown() {
    this.options.addOutputFormats(Arrays.asList("XML"));
    assertNotNull(this.testee.createListener());
  }

  @Test
  public void shouldSupportXMLAndCSV() {
    this.options.addOutputFormats(Arrays.asList("CSV", "XML"));
    assertNotNull(this.testee.createListener());
  }

  @Test(expected = PitError.class)
  public void shouldThrowErrorWhenRequestedListenerNotKnown() {
    this.options.addOutputFormats(Arrays.asList("unknown"));
    this.testee.createListener();
  }

  @Test
  public void shouldReturnADefaultJavaExecutableWhenNoneIsSpecified() {
    this.options.setJavaExecutable(null);
    File actual = new File(this.testee.getJavaExecutable().javaExecutable());
    if (System.getProperty("os.name").contains("Windows")) {
      actual = new File(actual.getPath() + ".exe");
    }
    assertTrue(actual.exists());
  }

  @Test
  public void shouldReturnSpecifiedJavaExecutableWhenOneSet() {
    this.options.setJavaExecutable("foo");
    assertEquals("foo", this.testee.getJavaExecutable().javaExecutable());
  }

  @Test
  public void shouldNotAllowUserToCalculateCoverageForCoreClasses() {
    this.options.setTargetClasses(Collections
        .singleton("java.Integer"));
    final CoverageOptions actual = this.testee.createCoverageOptions();
    assertFalse(actual.getFilter().test("java.Integer"));
  }

  @Test
  public void shouldNotAllowUserToCalculateCoverageForCoverageImplementation() {
    this.options.setTargetClasses(Collections
        .singleton("/org/pitest/coverage"));
    final CoverageOptions actual = this.testee.createCoverageOptions();
    assertFalse(actual.getFilter().test("org/pitest/coverage"));
  }

  @Test
  public void shouldDescribeActiveFeatures() {
    final Consumer<Feature> disabled = Mockito.mock(Consumer.class);
    final Consumer<Feature> enabled = Mockito.mock(Consumer.class);

    this.options.setFeatures(Arrays.asList("+FSTATI"));

    this.testee.describeFeatures(enabled, disabled);
    verify(enabled).accept(Feature.named("FSTATI"));
    verify(disabled, never()).accept(Feature.named("FSTATI"));
  }

  @Test
  public void shouldDescribeDisabledFeatures() {
    final Consumer<Feature> disabled = Mockito.mock(Consumer.class);
    final Consumer<Feature> enabled = Mockito.mock(Consumer.class);

    this.options.setFeatures(Arrays.asList("-FSTATI"));

    this.testee.describeFeatures(enabled, disabled);
    verify(enabled, never()).accept(Feature.named("FSTATI"));
    verify(disabled).accept(Feature.named("FSTATI"));
  }

  @Test
  public void shouldErrorWhenUnkownFeatureRequested() {
    this.options.setFeatures(Arrays.asList("+UNKOWN"));

    assertThatCode( () ->this.testee.checkRequestedFeatures())
            .hasMessageContaining(("UNKOWN"));

  }

  @Test
  public void shouldTreatFeaturesAsCaseInsensitive() {
    this.options.setFeatures(Arrays.asList("+feNUm"));

    assertThatCode( () ->this.testee.checkRequestedFeatures())
            .doesNotThrowAnyException();

  }

  @Test
  public void producesDefaultHistoryStore() {
    assertThat(this.testee.createHistory()).isInstanceOf(DefaultHistoryFactory.class);
  }

}
