package com.example.coverage.execute.samples.exceptions;

public class ThrowsExceptionTestee {
  public void foo() {
    CoveredBeforeExceptionTestee.bar();
    throwsException();
  }

  private void throwsException() {
    throw new RuntimeException();
  }
}