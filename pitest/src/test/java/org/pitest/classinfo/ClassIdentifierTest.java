package org.pitest.classinfo;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class ClassIdentifierTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(ClassIdentifier.class).verify();
  }

}
