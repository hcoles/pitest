package com.example.staticinitializers;

public class MethodsCalledFromInitializerAndElseWhere {

  static {
    mutateMe();
  }

  private static boolean mutateMe() {
    System.out.println("don't mutate");
    return true;
  }

  public static boolean alsoCalls() {
    return mutateMe();
  }

}
