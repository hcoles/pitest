package com.example.coverage.execute.samples.mutationMatrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class TestsForSimpleCalculator {

  @Test
  public void testSum() {
    assertEquals(3, SimpleCalculator.sum(2, 1));
  }

  @Test
  public void testSumWithNegativeNumber() {
	  assertEquals(1, SimpleCalculator.sum(4, -3));
  }

  @Test
  public void pseudoTestSum() {
    SimpleCalculator.sum(2, 1);
  }
}
