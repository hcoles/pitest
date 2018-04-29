package com.example.coverage.execute.samples.simple;

public class TesteeWithComplexConstructors {

  public int f;

  TesteeWithComplexConstructors(int a) {
    if (a > 10) {
      this.f = 11;
    } else {
      this.f = 42;
    }
  }
}
