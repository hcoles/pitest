package com.example.coverage.execute.samples.exceptions;

public class ThrowsExceptionFromLargeMethodTestee {
  int i;

  public int foo() {
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    this.i++;
    throwsException();
    return this.i;
  }

  private void throwsException() {
    throw new RuntimeException();
  }
}
