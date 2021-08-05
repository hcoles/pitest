package com.example;

public record CustomRecord(Long timeStamp, String data) {

  public CustomRecord() {
    this(1L, "");
  }

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
