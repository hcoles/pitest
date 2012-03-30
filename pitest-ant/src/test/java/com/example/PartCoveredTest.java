package com.example;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class PartCoveredTest {

  private PartCovered testee;

  @Before
  public void setUp() {
    testee = new PartCovered();
  }

  @Test
  public void fullyTestReturnsOne() {
    assertEquals(1, testee.returnsOneProperlytestedByTest());
  }

  @Test
  public void coverButDoNotTestReturnsThree() {
    testee.returnsThreeCoveredButNotTestedByTest();
  }

}
