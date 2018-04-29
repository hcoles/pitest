package com.example.staticinitializers;

public class MethodsCalledOnlyFromInitializer {

  static {
    dontMutate();
  }

  private static void dontMutate() {
    System.out.println("don't mutate");
  }

}
