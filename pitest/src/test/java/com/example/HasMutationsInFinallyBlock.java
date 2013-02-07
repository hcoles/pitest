package com.example;

public class HasMutationsInFinallyBlock {

  public int foo(int i) {
    try {
      System.out.println("don't optimise me away");
    } finally {
      i++;
    }
    return i;
  }

}
