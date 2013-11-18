package com.example.coverage.execute.samples.exceptions;

public class ThrowsExceptionInFinallyBlockTestee {

  public void foo() {
    try {
      throwsException();
    } finally {
      System.out.println("foo");
    }
  }

  private void throwsException() {
    throw new RuntimeException();
  }

}
