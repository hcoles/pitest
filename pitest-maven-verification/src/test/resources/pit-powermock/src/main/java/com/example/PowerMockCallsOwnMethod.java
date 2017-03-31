package com.example;

public class PowerMockCallsOwnMethod {
  public void call() {
    foo();
  }

  public static void foo() {

  }
  
  public int branchedCode(int anInt) {
    int ret = 10;
    if (anInt > 0) {
      ret = ret + anInt;
    }
    return ret;
  }
}
