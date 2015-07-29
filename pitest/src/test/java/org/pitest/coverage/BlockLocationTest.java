package org.pitest.coverage;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class BlockLocationTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(BlockLocation.class).verify();
  }

}
