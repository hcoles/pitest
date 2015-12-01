package com.example;

public class CoveredByMultipleThreads {

  public int lotsOfLinesOfCode(int i) {
    if ( i == 0 ) {
      return 1;
    }
    
    if ( i == 2 ) {
      return 42;
    }
    
    for ( int j = 0; j != 100; j++ ) {
       try {
        Thread.sleep(1);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }   
    }
    
    return i;
  }
  
}
