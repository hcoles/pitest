package com.example.java7;

public class HasIfOnAStringEqualityTestee {
  public String ifString(final String input) {
    final String result;

    if (input.equals("a")) {
      result = "A";
    } else if (input.equals("b")) {
      result = "B";
    } else if (input.equals("c")) {
      result = "C";
    } else {
      throw new IllegalArgumentException("Unsupported input");
    }

    return result;
  }

}
