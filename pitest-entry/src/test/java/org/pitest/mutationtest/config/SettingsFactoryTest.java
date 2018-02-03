package org.pitest.mutationtest.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.coverage.execute.CoverageOptions;
import org.pitest.coverage.export.NullCoverageExporter;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.engine.gregor.config.GregorEngineFactory;
import org.pitest.plugin.Feature;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.PitError;

public class SettingsFactoryTest {

  private final ReportOptions  options = new ReportOptions();

  private final PluginServices plugins = PluginServices.makeForContextLoader();

  private SettingsFactory      testee;

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
        .singleton("java/Integer"));
    final CoverageOptions actual = this.testee.createCoverageOptions();
    assertFalse(actual.getFilter().test("java/Integer"));
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
    final SideEffect1<Feature> disabled = Mockito.mock(SideEffect1.class);
    final SideEffect1<Feature> enabled = Mockito.mock(SideEffect1.class);

    this.options.setFeatures(Arrays.asList("+FSTATINIT"));

    this.testee.describeFeatures(enabled, disabled);
    verify(enabled).apply(Feature.named("FSTATINIT"));
    verify(disabled, never()).apply(Feature.named("FSTATINIT"));
  }

  @Test
  public void shouldDescribeDisabledFeatures() {
    final SideEffect1<Feature> disabled = Mockito.mock(SideEffect1.class);
    final SideEffect1<Feature> enabled = Mockito.mock(SideEffect1.class);

    this.options.setFeatures(Arrays.asList("-FSTATINIT"));

    this.testee.describeFeatures(enabled, disabled);
    verify(enabled, never()).apply(Feature.named("FSTATINIT"));
    verify(disabled).apply(Feature.named("FSTATINIT"));
  }

}
