package org.pitest.dependency;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class DependencyAccessTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(DependencyAccess.class).verify();
  }

}
