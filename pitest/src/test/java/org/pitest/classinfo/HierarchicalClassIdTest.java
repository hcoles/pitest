package org.pitest.classinfo;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Test;

public class HierarchicalClassIdTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(HierarchicalClassId.class).verify();
  }

}
