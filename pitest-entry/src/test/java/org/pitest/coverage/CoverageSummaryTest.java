package org.pitest.coverage;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

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

  @Test
  public void shouldCalculatePreciseCoverageAtPrecisionTwo() {
    assertThat(new CoverageSummary(1295, 796, 0).getCoverage(2))
        .isEqualByComparingTo(new BigDecimal("61.47"));
  }

  @Test
  public void shouldCalculatePreciseCoverageAtPrecisionZero() {
    assertThat(new CoverageSummary(100, 50, 0).getCoverage(0))
        .isEqualByComparingTo(new BigDecimal("50"));
  }

}
