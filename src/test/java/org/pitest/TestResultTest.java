package org.pitest;

import static org.junit.Assert.fail;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.pitest.testunit.TestUnitState;

public class TestResultTest {

  @Test
  public void testCanBeSerializedAndDeserialized() throws Exception {
    try {
      final TestResult testee = new TestResult(null, null,
          TestUnitState.FINISHED);
      SerializationUtils.clone(testee);
    } catch (final Throwable t) {
      fail();
    }
  }
}
