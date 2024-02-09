package com.example;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class AnotherTest {
  
  @Test
  public void aTest() {
    assertEquals(1, Covered.someCode(0));
  }

  @Test
  public void anotherTest() {
    assertEquals(1, Covered.someCode(1));
  }

  @Test
  public void dependsOnArgLine() {
    assertEquals("isSet", System.getProperty("MUST_BE_SET"));
    assertEquals("alsoSet", System.getProperty("MUST_ALSO_BE_SET"));
  }

}
