package com.example;

import static org.junit.Assert.assertEquals;

import org.junit.BeforeClass;
import org.junit.Test;

public class BeforeAfterClassTest {

  @BeforeClass
  public static void doSomething() {
    // well actually do nothing except be here
  }

  @Test
  public void shouldKillMutant1() {
    assertEquals(1, CoveredByABeforeAfterClass.returnOne());
  }

  @Test
  public void shouldKillMutantAgainButShouldNotBeRun() {
    assertEquals(1, CoveredByABeforeAfterClass.returnOne());
  }
}
