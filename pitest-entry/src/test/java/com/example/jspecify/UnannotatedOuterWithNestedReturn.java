package com.example.jspecify;

import java.util.concurrent.Callable;

public class UnannotatedOuterWithNestedReturn {

  public static class Nested implements Callable<Object> {
    @Override
    public Object call() {
      return "";
    }
  }
}
