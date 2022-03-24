package org.pitest;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;
import org.pitest.testapi.TestResult;


public class TestResultTest {

  @Test
  public void shouldObeyHashcodeEqualsContract()  {
    EqualsVerifier.forClass(TestResult.class)
            .verify();
  }

}
