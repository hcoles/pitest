package org.pitest.coverage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class CoverageSummaryTest {

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenNoLinesPresent() {
    assertThat(new CoverageSummary(0, 0, 0).getCoverage()).isEqualTo(100);
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenNoLinesCovered() {
    assertThat(new CoverageSummary(100, 0, 0).getCoverage()).isEqualTo(0);
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenAllLinesCovered() {
    assertThat(new CoverageSummary(100, 100, 0).getCoverage()).isEqualTo(100);
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenPartiallyCovered() {
    assertThat(new CoverageSummary(100, 50, 0).getCoverage()).isEqualTo(50);
  }

}
