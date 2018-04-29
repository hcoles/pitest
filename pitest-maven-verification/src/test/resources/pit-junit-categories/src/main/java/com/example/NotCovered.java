package com.example;

public class NotCovered {

  public static int someCode(int i) {
    if ( i == 0 ) {
      return 1;
    }
    
    if ( i == 2 ) {
      return 42;
    }
    
    return i;
  }
  
}
