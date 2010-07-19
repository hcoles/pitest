package org.pitest;

import static org.junit.Assert.fail;

import java.io.IOException;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.apache.commons.lang.SerializationUtils;
import org.junit.Test;
import org.pitest.testunit.TestUnitState;

public class TestResultTest {

  @Test
  public void testCanBeSerializedAndDeserialized() throws Exception {
    try {
      final TestResult testee = new TestResult((Description) null, null,
          TestUnitState.FINISHED);
      SerializationUtils.clone(testee);
    } catch (final Throwable t) {
      fail();
    }
  }

  @Test
  public void testEqualsContractKept() {
    EqualsVerifier.forClass(TestResult.class).withPrefabValues(Throwable.class,
        new IOException(), new UnsupportedOperationException()).verify();
  }

}
