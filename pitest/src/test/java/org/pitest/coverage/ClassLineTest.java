package org.pitest.coverage;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ClassLineTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(ClassLine.class).verify();
  }

}
