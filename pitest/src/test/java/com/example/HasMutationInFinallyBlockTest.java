package com.example;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class HasMutationInFinallyBlockTest {

  @Test
  public void testIncrementsI() {
    HasMutationsInFinallyBlock testee = new HasMutationsInFinallyBlock();
    assertEquals(2,testee.foo(1));
  }
  
}
