package com.example.coverage.execute.samples.exceptions;

import org.junit.Test;

public class TestsClassWithException {
  @Test
  public void test() {
    final ThrowsExceptionTestee t = new ThrowsExceptionTestee();
    t.foo();
  }
}