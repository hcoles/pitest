/**
 * 
 */
package org.pitest;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pitest.testunit.TestUnitState;

/**
 * @author henry
 * 
 */
public class ResultClassifierTest {

  private DefaultResultClassifier testee;
  private Description      description;

  @Before
  public void setUp() {
    this.testee = new DefaultResultClassifier();
    this.description = new Description(null, null, null);
  }

  @Test
  public void testStartedTest() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.description, null, TestUnitState.STARTED));
    assertEquals(ResultType.STARTED, actual);
  }

  @Test
  public void testJavaAssertionClassifiedAsFailed() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.description, new java.lang.AssertionError(),
        TestUnitState.FINISHED));
    assertEquals(ResultType.FAIL, actual);
  }

  @Test
  public void testJUnitFrameworkAssertionFailedErrorClassifiedAsFailed() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.description, new junit.framework.AssertionFailedError(),
        TestUnitState.FINISHED));
    assertEquals(ResultType.FAIL, actual);
  }

  @Test
  public void testOtherThrowableClassifiedAsError() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.description, new NullPointerException(), TestUnitState.FINISHED));
    assertEquals(ResultType.ERROR, actual);
  }

  @Test
  public void testNoThrowableClassifiedAsPass() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.description, null, TestUnitState.FINISHED));
    assertEquals(ResultType.PASS, actual);
  }

  @Test
  public void testNotRunClassifiedAsSkipped() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.description, null, TestUnitState.NOT_RUN));
    assertEquals(ResultType.SKIPPED, actual);
  }

}
