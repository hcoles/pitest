package com.example.java7;

public class HasSwitchOnStringTestee {

  public String switchIntegerAndThenSwitchString(final Integer input) {
    final String partial;

    switch (input) {
    case 1:
      partial = "A";
      break;
    case 2:
      partial = "B";
      break;
    case 3:
      partial = "C";
      break;
    default:
      throw new IllegalArgumentException("Unsupported input");
    }

    final String result;

    switch (partial) {
    case "A":
      result = "A1";
      break;
    case "B":
      result = "B1";
      break;
    case "C":
      result = "C1";
      break;
    default:
      throw new IllegalArgumentException("Unsupported input");
    }

    return result;
  }

  public String switchString(final String input) {

    final String result;

    switch (input) {
    case "a":
      result = "A";
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
