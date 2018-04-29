package org.pitest.coverage;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class TestInfoTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(TestInfo.class).verify();
  }
}
