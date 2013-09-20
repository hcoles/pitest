/**
 * 
 */
package org.pitest.execute;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pitest.DescriptionMother;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnitState;

/**
 * @author henry
 * 
 */
public class ResultClassifierTest {

  private DefaultResultClassifier testee;
  private Description             description;

  @Before
  public void createTestee() {
    this.testee = new DefaultResultClassifier();
    this.description = DescriptionMother.createEmptyDescription("foo");
  }

  @Test
  public void shouldClassifyTestResultsWithStatusOfStartedAsStarted() {
    final ResultType actual = this.testee.classify(new TestResult(
        this.description, null, TestUnitState.STARTED));
    assertEquals(ResultType.STARTED, actual);
  }

  @Test
  public void shouldClassifyJavaAssertionsAsFailed() {
    final ResultType actual = this.testee.classify(new TestResult(
        this.description, new java.lang.AssertionError(),
        TestUnitState.FINISHED));
    assertEquals(ResultType.FAIL, actual);
  }

  @Test
  public void shouldClassifyJUnitFrameworkAssertionFailedErrorsAsFailed() {
    final ResultType actual = this.testee.classify(new TestResult(
        this.description, new junit.framework.AssertionFailedError(),
        TestUnitState.FINISHED));
    assertEquals(ResultType.FAIL, actual);
  }

  @Test
  public void shouldClassifyThrowablesAtErrors() {
    final ResultType actual = this.testee.classify(new TestResult(
        this.description, new NullPointerException(), TestUnitState.FINISHED));
    assertEquals(ResultType.ERROR, actual);
  }

  @Test
  public void shouldClassifyResultsWithoutThrowablesAsPass() {
    final ResultType actual = this.testee.classify(new TestResult(
        this.description, null, TestUnitState.FINISHED));
    assertEquals(ResultType.PASS, actual);
  }

  @Test
  public void shouldClassifyResultsThatWereNotRunAsSkipped() {
    final ResultType actual = this.testee.classify(new TestResult(
        this.description, null, TestUnitState.NOT_RUN));
    assertEquals(ResultType.SKIPPED, actual);
  }

}
