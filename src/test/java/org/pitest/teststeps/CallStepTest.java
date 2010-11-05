package org.pitest.teststeps;

import static org.junit.Assert.assertSame;

import org.junit.Before;
import org.junit.Test;
import org.pitest.TestMethod;

public class CallStepTest {

  private TestMethod testMethod;

  @Before
  public void setUp() throws Exception {
    this.testMethod = new TestMethod(this.getClass().getMethod("testMethod"));
  }

  @Test
  public void testReturnsSuppliedObject() {
    final CallStep testee = new CallStep(this.testMethod);
    assertSame(this, testee.execute(this.getClass().getClassLoader(), null,
        this));
  }

  public void testMethod() {

  };

}
