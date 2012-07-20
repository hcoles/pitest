package com.example;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JUnitThreeSuite extends TestCase {
  public JUnitThreeSuite(String testName) {
    super(testName);
  }

  public static junit.framework.Test suite() {
    TestSuite suite = new TestSuite();
    suite.addTest(JUnitThreeSubSuite.suite());
    return suite;
  }

}