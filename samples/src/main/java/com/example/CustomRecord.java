package com.example;

public record CustomRecord(Long timeStamp, String data) {
  public String toString() {
    return "overridden";
  }

  public String data() {
    System.out.println("side effect");
    return data;
  }

  public int extraMethod() {
    return 42;
  }
}
