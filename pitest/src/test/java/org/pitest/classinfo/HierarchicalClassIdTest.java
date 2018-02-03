package org.pitest.classinfo;

import org.junit.Test;

import nl.jqno.equalsverifier.EqualsVerifier;

public class HierarchicalClassIdTest {

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(HierarchicalClassId.class).verify();
  }

}
