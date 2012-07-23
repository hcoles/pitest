package org.pitest.coverage.interprocess;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.Description;
import org.pitest.boot.CodeCoverageStore;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.instrument.protocol.Id;
import org.pitest.util.SafeDataInputStream;


public class ReceiveTest {
  
  private Receive testee;
  
  private SideEffect1<CoverageResult>  handler;
  
  private CoverageResult result;
  
  private Description description;
  
  @Mock
  private SafeDataInputStream is;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    handler = stubHandler();
    testee = new Receive(handler);
    description =  new Description("foo","bar");
  }
  
  
  
  private SideEffect1<CoverageResult> stubHandler() {
    return new SideEffect1<CoverageResult>() {
      public void apply(CoverageResult a) {
        result = a;
      }
    };
  }


  @Test
  public void shouldReportNoCoverageWhenNoTestsRun() {
    testee.apply(Id.DONE, is);
    assertNull(result);
  }
  
  @Test
  public void shouldReportCoverageWhenLineHitByTest() {
    int lineNumber = 42;
    recordTestCoverage(0, 0, lineNumber, true);
    
    assertEquals(1,result.getCoverage().size());
    assertEquals(Collections.singleton(lineNumber),result.getCoverage().iterator().next().getUniqueVisitedLines());
  }
  
  @Test
  public void shouldReportWhenTestFails() {
    recordTestCoverage(0, 0, 0, false);
    assertEquals(false,result.isGreenTest());
  }
  
  @Test
  public void shouldReportWhenTestPasses() {
    recordTestCoverage(0, 0, 0, true);
    assertEquals(true,result.isGreenTest());
  }

  @Test
  public void shouldReportExectionTime() {
    int executionTime = 1000;
    recordTestCoverage(executionTime, 0, 0, true);
    
    assertEquals(executionTime,result.getExecutionTime());
  }


  private void recordTestCoverage(int executionTime,
      int classId, int lineNumber, boolean testPassed) {
    when(is.readInt()).thenReturn(classId, executionTime);
    when(is.readString()).thenReturn("foo");
    testee.apply(Id.CLAZZ, is);
    
    when(is.read(Description.class)).thenReturn(description);
    when(is.readLong()).thenReturn(1l,CodeCoverageStore.encode(classId, lineNumber));    
    when(is.readBoolean()).thenReturn(testPassed);
    testee.apply(Id.OUTCOME, is);
  }

}
