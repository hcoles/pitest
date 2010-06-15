/**
 * 
 */
package org.pitest;

import static org.junit.Assert.assertEquals;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.pitest.extension.TestStep;
import org.pitest.extension.TestUnit;
import org.pitest.testunit.SteppedTestUnit;
import org.pitest.testunit.TestUnitState;

/**
 * @author henry
 * 
 */
public class ResultClassifierTest {

  private ResultClassifier testee;
  private TestUnit         exampleTestUnit;

  @Before
  public void setUp() {
    this.testee = new ResultClassifier();
    this.exampleTestUnit = new SteppedTestUnit(
        new Description(null, null, null), Collections.<TestStep> emptyList());
  }

  @Test
  public void testStartedTest() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.exampleTestUnit, null, TestUnitState.STARTED));
    assertEquals(ResultType.STARTED, actual);
  }

  @Test
  public void testJavaAssertionClassifiedAsFailed() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.exampleTestUnit, new java.lang.AssertionError(),
        TestUnitState.FINISHED));
    assertEquals(ResultType.FAIL, actual);
  }

  @Test
  public void testJUnitFrameworkAssertionFailedErrorClassifiedAsFailed() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.exampleTestUnit, new junit.framework.AssertionFailedError(),
        TestUnitState.FINISHED));
    assertEquals(ResultType.FAIL, actual);
  }

  @Test
  public void testOtherThrowableClassifiedAsError() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.exampleTestUnit, new NullPointerException(),
        TestUnitState.FINISHED));
    assertEquals(ResultType.ERROR, actual);
  }

  @Test
  public void testNoThrowableClassifiedAsPass() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.exampleTestUnit, null, TestUnitState.FINISHED));
    assertEquals(ResultType.PASS, actual);
  }

  @Test
  public void testNotRunClassifiedAsSkipped() {
    final ResultType actual = this.testee.apply(new TestResult(
        this.exampleTestUnit, null, TestUnitState.NOT_RUN));
    assertEquals(ResultType.SKIPPED, actual);
  }

}
