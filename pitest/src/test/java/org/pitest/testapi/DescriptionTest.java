package org.pitest.testapi;

import nl.jqno.equalsverifier.EqualsVerifier;
import org.junit.Test;

public class DescriptionTest {


  @Test
  public void shouldObeyHashcodeEqualsContract()  {
    EqualsVerifier.forClass(Description.class)
            .verify();
  }

}
