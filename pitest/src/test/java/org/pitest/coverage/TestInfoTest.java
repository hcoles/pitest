package org.pitest.coverage;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class TestInfoTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(TestInfo.class).verify();
  }
}
