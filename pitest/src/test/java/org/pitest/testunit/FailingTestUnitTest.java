package org.pitest.testunit;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.extension.ResultCollector;
import org.pitest.internal.IsolationUtils;

public class FailingTestUnitTest {

  private FailingTestUnit testee;

  @Mock
  ResultCollector         rc;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void shouldReportStartAndFailureWhenRun() {

    final Description description = new Description("foo",
        FailingTestUnitTest.class, null);

    this.testee = new FailingTestUnit(description, "foo");
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyStart(eq(description));
    verify(this.rc).notifyEnd(eq(description), any(AssertionError.class));
  }

}
