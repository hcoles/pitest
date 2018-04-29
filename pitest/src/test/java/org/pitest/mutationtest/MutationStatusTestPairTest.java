package org.pitest.mutationtest;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class MutationStatusTestPairTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutationStatusTestPair.class).verify();
  }

}
