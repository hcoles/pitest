package com.example.coverage.execute.samples.simple;

import org.junit.Test;

public class TesteeWithComplexConstructorsTest {

  @Test
  public void testLow() {
    new TesteeWithComplexConstructors(1);
  }

  @Test
  public void testHigh() {
    new TesteeWithComplexConstructors(100);
  }

}
