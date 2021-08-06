package com.example;

public class NotARecord {
  private final String value;

  public NotARecord(String value) {
    this.value = value;
  }

  public final String value() {
   return value;
  } 

}
