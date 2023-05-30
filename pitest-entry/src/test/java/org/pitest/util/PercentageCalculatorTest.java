package org.pitest.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.pitest.util.PercentageCalculator.getPercentage;

public class PercentageCalculatorTest {
  @Test
  public void shouldNotHaveHundredPercentIfNotAll() {
    assertEquals(99, getPercentage(2000, 1999));
  }

  @Test
  public void shouldHaveHundredPercentIfAll() {
    assertEquals(100, getPercentage(2000, 2000));
  }

  @Test
  public void shouldHaveHundredPercentIfNoTotal() {
    assertEquals(100, getPercentage(0, 0));
  }
}
