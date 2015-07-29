package org.pitest.classinfo;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class ClassIdentifierTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(ClassIdentifier.class).verify();
  }

}
