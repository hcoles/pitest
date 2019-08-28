package com.example.blockcoverage;

import org.junit.Test;

public class HasExceptionsTest {
  @Test
  public void testCallsMethodThrowsException() {
    try {
      HasExceptionsTestee.foo();
    } catch (NullPointerException ex) {
      //nop
    }
  }
}
