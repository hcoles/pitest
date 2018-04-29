package com.example;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class PartCoveredTest {

  private PartCovered testee;

  @Before
  public void setUp() {
    this.testee = new PartCovered();
  }

  @Test
  public void fullyTestReturnsOne() {
    assertEquals(1, this.testee.returnsOneProperlytestedByTest());
  }

  @Test
  public void coverButDoNotTestReturnsThree() {
    this.testee.returnsThreeCoveredButNotTestedByTest();
  }

}
