package org.pitest.coverage.execute;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.coverage.CoverageResult;
import org.pitest.functional.SideEffect1;
import org.pitest.testapi.Description;
import org.pitest.util.Id;
import org.pitest.util.SafeDataInputStream;

import sun.pitest.CodeCoverageStore;

// does this test add any value?
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
    return a -> ReceiveTest.this.result = a;
  }

  @Test
  public void shouldReportNoCoverageWhenNoTestsRun() {
    this.testee.apply(Id.DONE, this.is);
    assertNull(this.result);
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

  private void recordTestCoverage(final int executionTime, final int classId,
      final int probeNumber, final boolean testPassed) {
    when(this.is.readInt()).thenReturn(classId, executionTime);
    when(this.is.readString()).thenReturn("foo");
    this.testee.apply(Id.CLAZZ, this.is);

    when(this.is.read(Description.class)).thenReturn(this.description);
    when(this.is.readInt()).thenReturn(1);
    when(this.is.readLong()).thenReturn(1l,
        CodeCoverageStore.encode(classId, probeNumber));
    when(this.is.readBoolean()).thenReturn(testPassed);
    this.testee.apply(Id.OUTCOME, this.is);
  }

}
