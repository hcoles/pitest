package org.pitest.mutationtest;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pitest.coverage.export.NullCoverageExporter;

public class SettingsFactoryTest {

  private final ReportOptions options = new ReportOptions();

  private SettingsFactory     testee;

  @Before
  public void setUp() {
    this.testee = new SettingsFactory(this.options);
  }

  @Test
  public void shouldReturnANullCoverageExporterWhenOptionSetToFalse() {
    this.options.setExportLineCoverage(false);
    assertTrue(this.testee.createCoverageExporter() instanceof NullCoverageExporter);
  }

}
