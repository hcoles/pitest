package org.pitest.mutationtest;

import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pitest.coverage.export.NullCoverageExporter;

public class SettingsFactoryTest {

  private ReportOptions options = new ReportOptions();
  
  private SettingsFactory testee;
  
  @Before
  public void setUp() {
    testee =  new SettingsFactory(options);
  }
  
  @Test
  public void shouldReturnANullCoverageExporterWhenOptionSetToFalse() {
    options.setExportLineCoverage(false);
    assertTrue(testee.createCoverageExporter() instanceof NullCoverageExporter); 
  }

}
