package com.example.coverage.execute.samples.exceptions;

import org.junit.Test;

public class TestThrowsExceptionInFinallyBlock {

  @Test
  public void test() {
    final ThrowsExceptionInFinallyBlockTestee t = new ThrowsExceptionInFinallyBlockTestee();
    t.foo();
  }

}
