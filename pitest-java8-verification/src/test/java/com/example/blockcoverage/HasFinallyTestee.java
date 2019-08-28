package com.example.blockcoverage;

public class HasFinallyTestee {

  public static boolean methodWithFinally(boolean bailEarly) {
    int x = 0;
    int y = 0;
    try {
      if (bailEarly)
        return true;
    } finally {
      x++;
      x--;
    }
    return false;
  }
}
