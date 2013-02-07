package com.example;

import junit.framework.TestSuite;

public class JUnitThreeSubSuite {

  public static junit.framework.Test suite() {
    final TestSuite suite = new TestSuite();
    suite.addTest(new JUnitThreeTest("testSomething"));
    return suite;
  }

}
