package com.example;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


public class PartiallyTestedTest {
  
  @Test
  public void testSomeStuff() {
    Comparable<Integer> sideEffect = new     Comparable<Integer>() {

      public int compareTo(Integer arg0) {
        return 0;
      }
      
    };
    
    PartiallyTested testee = new PartiallyTested(sideEffect);
    assertEquals(200,testee.doLotsOfThings(100));
  }

}
