package org.pitest.mutationtest;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class MutationConfigTest {

  @Test
  public void shouldKeepEqualsContract() {
    EqualsVerifier.forClass(MutationConfig.class).verify();
  }

}
