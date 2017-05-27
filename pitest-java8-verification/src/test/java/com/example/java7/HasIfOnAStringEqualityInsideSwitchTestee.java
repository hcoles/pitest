package com.example.java7;

public class HasIfOnAStringEqualityInsideSwitchTestee {
  public String ifStringInsideSwitch(final String input, final String input2) {
    final String result;

    switch (input) {
    case "a":
      if (input2.equals("a")) {
        result = "A";
      } else {
        result = "AX";
      }
      break;
    case "b":
      result = "B";
      break;
    case "c":
      result = "C";
      break;
    default:
      throw new IllegalArgumentException("Unsupported input");
    }

    return result;
  }

}
