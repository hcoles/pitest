package com.example.coverage.execute.samples.simple;

public class TesteeWithMultipleLines {

  public int bar(int i, int j) {
    i = i + j;
    j = +j + j;
    i = j + i;

    return i++;
  }

  public int foo(final int i) {
    int j = 0;
    try {
      j = j + 1;
      j = Math.max(i, j);
      return j;
    } catch (final RuntimeException ex) {
      return 1;
    }
  }

}
