package com.example;

import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ATest {
  
  @Test
  public void aTest() {
    assertEquals(1, NotCovered.someCode(0));
  }

  @Test
  public void anotherTest() {
    assertEquals(42, NotCovered.someCode(2));
  }

}
