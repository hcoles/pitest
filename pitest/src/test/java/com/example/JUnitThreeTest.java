package com.example;

import junit.framework.TestCase;

public class JUnitThreeTest extends TestCase {

  public JUnitThreeTest(final String name) {
    super(name);
  }

  public void testSomething() {
    assertEquals(1, new CoveredByJUnitThreeSuite().foo());
  }

}