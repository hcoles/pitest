package com.example.coverage.execute.samples.exceptions;

import org.junit.Test;

public class TestThrowsExceptionFromLargeMethodTestee {

  @Test
  public void test() {
    ThrowsExceptionFromLargeMethodTestee testee = new ThrowsExceptionFromLargeMethodTestee();
    testee.foo();
  }

}
