package org.pitest.coverage;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class ClassLineTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(ClassLine.class).verify();
  }

}
