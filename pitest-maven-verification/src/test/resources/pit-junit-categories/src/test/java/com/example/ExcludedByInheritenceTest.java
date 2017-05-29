package com.example;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ExcludedByInheritenceTest extends ATest {
  @Test
  public void aTest() {
    assertEquals(1, NotCovered.someCode(0));
  }

  @Test
  public void anotherTest() {
    assertEquals(42, NotCovered.someCode(2));
  }

}
