package com.example;

public class PowerMockCallsOwnMethod {
  public void call() {
    foo();
  }

  public static void foo() {

  }
}
