package org.pitest.util;

import org.junit.Test;

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
}
