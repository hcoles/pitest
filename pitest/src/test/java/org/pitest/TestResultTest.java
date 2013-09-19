package org.pitest;

import static org.junit.Assert.fail;

import org.junit.Test;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnitState;
import org.pitest.util.IsolationUtils;

public class TestResultTest {

  @Test
  public void shouldCloneViaXStreamWithoutError() throws Exception {
    try {
      final TestResult testee = new TestResult((Description) null, null,
          TestUnitState.FINISHED);
      IsolationUtils.clone(testee);
    } catch (final Throwable t) {
      fail();
    }
  }

}
