package org.pitest.junit;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.DescriptionMother;
import org.pitest.extension.ResultCollector;

public class AdaptingRunListenerTest {

  private AdaptingRunListener    testee;

  private org.pitest.Description pitDescription;

  private Throwable              throwable;

  @Mock
  private Description            junitDesc;

  @Mock
  private ResultCollector        rc;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.throwable = new NullPointerException();
    this.pitDescription = DescriptionMother.createEmptyDescription("foo");
    this.testee = new AdaptingRunListener(this.pitDescription, this.rc);
  }

  @Test
  public void shouldReportExceptionOnFailure() throws Exception {
    this.testee.testFailure(new Failure(this.junitDesc, this.throwable));
    verify(this.rc).notifyEnd(this.pitDescription, this.throwable);
  }

  @Test
  public void shouldNotReportTestEndWithoutErrorAfterFailure() throws Exception {
    this.testee.testFailure(new Failure(this.junitDesc, this.throwable));
    this.testee.testFinished(this.junitDesc);
    verify(this.rc, never()).notifyEnd(this.pitDescription);
  }

  @Test
  public void shouldTreatAssumptionFailureAsSuccess() throws Exception {
    this.testee.testAssumptionFailure(new Failure(this.junitDesc,
        this.throwable));
    verify(this.rc, never()).notifyEnd(this.pitDescription, this.throwable);
    this.testee.testFinished(this.junitDesc);
    verify(this.rc).notifyEnd(this.pitDescription);
  }

  @Test
  public void shouldReportIgnoredTestsAsSkipped() throws Exception {
    this.testee.testIgnored(this.junitDesc);
    verify(this.rc).notifySkipped(this.pitDescription);
  }

  @Test
  public void shouldNotReportTestEndWithoutErrorAfterIgnore() throws Exception {
    this.testee.testIgnored(this.junitDesc);
    this.testee.testFinished(this.junitDesc);
    verify(this.rc, never()).notifyEnd(this.pitDescription);
  }

  @Test
  public void shouldReportStartedTests() throws Exception {
    this.testee.testStarted(this.junitDesc);
    verify(this.rc).notifyStart(this.pitDescription);
  }

}
