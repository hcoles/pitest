package org.pitest.coverage.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Collections;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.coverage.CoverageResult;
import org.pitest.coverage.execute.Receive;
import org.pitest.functional.SideEffect1;
import org.pitest.testapi.Description;
import org.pitest.util.Id;
import org.pitest.util.SafeDataInputStream;

import sun.pitest.CodeCoverageStore;

public class ReceiveTest {

  private Receive                     testee;

  private SideEffect1<CoverageResult> handler;

  private CoverageResult              result;

  private Description                 description;

  @Mock
  private SafeDataInputStream         is;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.handler = stubHandler();
    this.testee = new Receive(this.handler);
    this.description = new Description("foo", "bar");
  }

  private SideEffect1<CoverageResult> stubHandler() {
    return new SideEffect1<CoverageResult>() {
      public void apply(final CoverageResult a) {
        ReceiveTest.this.result = a;
      }
    };
  }

  @Test
  public void shouldReportNoCoverageWhenNoTestsRun() {
    this.testee.apply(Id.DONE, this.is);
    assertNull(this.result);
  }

  @Test
  public void shouldReportCoverageWhenLineHitByTest() {
    final int lineNumber = 42;
    recordTestCoverage(0, 0, lineNumber, true);

    assertEquals(1, this.result.getCoverage().size());
    assertEquals(Collections.singleton(lineNumber), this.result.getCoverage()
        .iterator().next().getUniqueVisitedLines());
  }

  @Test
  public void shouldReportWhenTestFails() {
    recordTestCoverage(0, 0, 0, false);
    assertEquals(false, this.result.isGreenTest());
  }

  @Test
  public void shouldReportWhenTestPasses() {
    recordTestCoverage(0, 0, 0, true);
    assertEquals(true, this.result.isGreenTest());
  }

  @Test
  public void shouldReportExectionTime() {
    final int executionTime = 1000;
    recordTestCoverage(executionTime, 0, 0, true);

    assertEquals(executionTime, this.result.getExecutionTime());
  }

  private void recordTestCoverage(final int executionTime, final int classId,
      final int lineNumber, final boolean testPassed) {
    when(this.is.readInt()).thenReturn(classId, executionTime);
    when(this.is.readString()).thenReturn("foo");
    this.testee.apply(Id.CLAZZ, this.is);

    when(this.is.read(Description.class)).thenReturn(this.description);
    when(this.is.readLong()).thenReturn(1l,
        CodeCoverageStore.encode(classId, lineNumber));
    when(this.is.readBoolean()).thenReturn(testPassed);
    this.testee.apply(Id.OUTCOME, this.is);
  }

}
