package org.pitest.simpletest;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class TestMethodTest {

  @Test
  public void shouldObeyHashcodeEqualsContract()  {
    EqualsVerifier.forClass(TestMethod.class)
            .verify();
  }

}
