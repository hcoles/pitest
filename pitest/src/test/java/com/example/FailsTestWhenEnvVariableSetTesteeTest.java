package com.example;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FailsTestWhenEnvVariableSetTesteeTest {

  @Test
  public void testNotCurrentlyFalse() {
    final FailsTestWhenEnvVariableSetTestee testee = new FailsTestWhenEnvVariableSetTestee();
    assertTrue(testee.returnTrue());
  }
}
