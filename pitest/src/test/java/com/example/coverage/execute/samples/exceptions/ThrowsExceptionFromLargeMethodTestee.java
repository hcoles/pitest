package com.example.coverage.execute.samples.exceptions;

public class ThrowsExceptionFromLargeMethodTestee {
  int i;
  
  public int foo() {
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      i++;
      throwsException();
      return i;
  }

  private void throwsException() {
    throw new RuntimeException();
  }
}
