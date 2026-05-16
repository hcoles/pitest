package org.pitest.util;

import org.junit.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.pitest.util.PercentageCalculator.getPercentage;

public class PercentageCalculatorTest {
  @Test
  public void shouldNotHaveHundredPercentIfNotAll() {
    assertThat(getPercentage(2000, 1999)).isEqualTo(99);
  }

  @Test
  public void shouldHaveHundredPercentIfAll() {
    assertThat(getPercentage(2000, 2000)).isEqualTo(100);
  }

  @Test
  public void shouldHaveHundredPercentIfNoTotal() {
    assertThat(getPercentage(0, 0)).isEqualTo(100);
  }

  @Test
  public void shouldReturnBigDecimalMatchingIntAtPrecisionZero() {
    assertThat(getPercentage(1295, 796, 0)).isEqualByComparingTo(new BigDecimal("61"));
  }

  @Test
  public void shouldReturnPreciseBigDecimalAtPrecisionTwo() {
    assertThat(getPercentage(1295, 796, 2)).isEqualByComparingTo(new BigDecimal("61.47"));
  }

  @Test
  public void shouldCapAtScaledMaxWhenNotTrulyHundredPercent() {
    assertThat(getPercentage(2000, 1999, 0)).isEqualByComparingTo(new BigDecimal("99"));
    assertThat(getPercentage(2000, 1999, 2)).isEqualByComparingTo(new BigDecimal("99.95"));
  }

  @Test
  public void shouldReturnHundredAtPrecisionWhenAllCovered() {
    assertThat(getPercentage(2000, 2000, 2)).isEqualByComparingTo(new BigDecimal("100.00"));
  }

  @Test
  public void shouldReturnHundredAtPrecisionWhenTotalIsZero() {
    assertThat(getPercentage(0, 0, 2)).isEqualByComparingTo(new BigDecimal("100.00"));
  }

  @Test
  public void shouldReturnZeroAtPrecisionWhenActualIsZero() {
    assertThat(getPercentage(100, 0, 2)).isEqualByComparingTo(new BigDecimal("0.00"));
  }

  @Test
  public void shouldWorkWithHighPrecision() {
    BigDecimal result = getPercentage(1295, 796, 7);
    assertThat(result).isEqualByComparingTo(new BigDecimal("61.4671815"));
  }
}
