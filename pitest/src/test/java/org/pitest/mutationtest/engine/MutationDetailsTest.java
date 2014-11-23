package org.pitest.mutationtest.engine;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class MutationDetailsTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutationDetails.class).verify();
  }

}
