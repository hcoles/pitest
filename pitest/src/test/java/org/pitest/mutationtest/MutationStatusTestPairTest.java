package org.pitest.mutationtest;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class MutationStatusTestPairTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutationStatusTestPair.class).verify();
  }

}
