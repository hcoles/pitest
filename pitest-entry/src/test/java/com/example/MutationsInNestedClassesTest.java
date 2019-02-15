package com.example;

import static org.junit.Assert.assertFalse;

import org.pitest.simpletest.TestAnnotationForTesting;

public class MutationsInNestedClassesTest {

  @TestAnnotationForTesting
  public void test_foo_and_bar() {
    if (!MutationsInNestedClasses.Bar.barMethod()) {
      System.err.println("Mutation found in bar");
    }
    if (!MutationsInNestedClasses.Foo.fooMethod()) {
      System.err.println("Mutation found in foo");
    }

    assertFalse(!MutationsInNestedClasses.Bar.barMethod()
        && !MutationsInNestedClasses.Foo.fooMethod());

  }
}
