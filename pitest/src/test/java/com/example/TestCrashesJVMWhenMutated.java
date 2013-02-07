package com.example;

import org.junit.Test;

public class TestCrashesJVMWhenMutated {

  @Test
  public void runCrashMethodButDontHitCrashCode() {
    CrashesJVMWhenMutated.crashJVM(42);
  }

}
