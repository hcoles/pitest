package com.example.coverage.execute.samples.simple;

public class ThreeMultiLineBlocks {
  int foo(int i) {
    System.out.println("foo");
    if (i > 30) {
      System.out.println("foo");
      return 1;
    }
    System.out.println("foo");
    return 2;
  }
}
