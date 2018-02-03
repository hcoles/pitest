package org.pitest.coverage.analysis;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class BlockTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(Block.class).verify();
  }

}
