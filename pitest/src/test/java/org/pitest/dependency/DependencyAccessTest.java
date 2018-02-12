package org.pitest.dependency;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class DependencyAccessTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(DependencyAccess.class).verify();
  }

}
