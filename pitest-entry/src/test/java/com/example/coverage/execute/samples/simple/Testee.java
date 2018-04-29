package com.example.coverage.execute.samples.simple;

public class Testee implements Runnable {
  public void foo() {
  }

  public void bar() {

  }

  @Override
  public void run() {
    new Testee2().bar();
  }

}