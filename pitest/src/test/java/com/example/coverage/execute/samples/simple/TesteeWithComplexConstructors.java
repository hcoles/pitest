package com.example.coverage.execute.samples.simple;

public class TesteeWithComplexConstructors {

  public int f;
  
  TesteeWithComplexConstructors(int a) {
    if ( a > 10 ) {
      f = 11;
    } else {
      f = 42;
    }
  }
}
