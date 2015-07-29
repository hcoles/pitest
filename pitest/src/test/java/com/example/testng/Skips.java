package com.example.testng;

import org.testng.SkipException;

public class Skips {

  @org.testng.annotations.Test()
  public void skip() {
    throw new SkipException("skipping");
  }
}
