package com.example.coverage.execute.samples.simple;

public interface TesteeThatIsAnInterface {

  default void foo() {
    System.out.println("boo");
  }

}
