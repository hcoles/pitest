package org.pitest.coverage;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CoverageSummaryTest {

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenNoLinesPresent() {
    assertEquals(100, new CoverageSummary(0, 0).getCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenNoLinesCovered() {
    assertEquals(0, new CoverageSummary(100, 0).getCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenAllLinesCovered() {
    assertEquals(100, new CoverageSummary(100, 100).getCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenPartiallyCovered() {
    assertEquals(50, new CoverageSummary(100, 50).getCoverage());
  }

}
