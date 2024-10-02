package org.example2;

import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNotNull;

import org.junit.Test;

public class SystemUnderTestTest {

  @Test
  public void testGetNumber() {
    SystemUnderTest sut = new SystemUnderTest();

    int result = sut.getNumber();
    assertTrue(result == -25);
  }
  
  
}