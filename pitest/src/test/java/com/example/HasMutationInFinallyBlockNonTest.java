package com.example;

import org.junit.Test;

public class HasMutationInFinallyBlockNonTest {

  @Test
  public void testIncrementsI() {
    HasMutationsInFinallyBlock testee = new HasMutationsInFinallyBlock();
    testee.foo(1); // cover but don't test
  }
  
}
