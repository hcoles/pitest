package com.example;

import org.junit.Test;


public class ExecutedByTestsButNotActuallyTestedByThemTest {

  @Test
  public void doesntReallyTestAnything() {
    ExecutedByTestsButNotActuallyTestedByThem testee = new ExecutedByTestsButNotActuallyTestedByThem();
    testee.returnOne();
  }
  
}
