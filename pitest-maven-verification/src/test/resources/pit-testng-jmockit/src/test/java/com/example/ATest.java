package com.example;

import java.util.Arrays;
import java.util.Collection;

import static org.testng.Assert.assertEquals;

@org.testng.annotations.Test
public class ATest {
  
  public void aTest() {
    assertEquals(1, Covered.someCode(0));
  }

  public void anotherTest() {
    assertEquals(42, Covered.someCode(2));
  }

}
