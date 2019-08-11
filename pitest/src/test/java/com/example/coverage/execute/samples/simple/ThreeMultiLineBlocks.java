package com.example.coverage.execute.samples.simple;

public class ThreeMultiLineBlocks {
  int foo(int i) {
    i++;
    System.out.println("foo");
    if (i > 30) {
      i++;
      System.out.println("foo");
      return 1;
    }
    i++;
    System.out.println("foo");
    return 2;
  }
}
