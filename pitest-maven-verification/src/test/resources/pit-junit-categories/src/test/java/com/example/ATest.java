package com.example;

import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.experimental.categories.Category;

import static org.junit.Assert.assertEquals;

@Category(ExcludeMe.class)
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
