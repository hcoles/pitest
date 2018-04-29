package org.pitest.coverage;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class BlockLocationTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(BlockLocation.class).verify();
  }

}
