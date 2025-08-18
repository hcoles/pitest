package com.example;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class MultiThreadedTest {
  
  private final int i;

  @Parameters
  public static Collection<Object[]> data() {
    Object[][] data = new Object[][] { { 1 }, { 2 }, { 3 }, { 4 }, { 5 } ,{ 6 }, { 7 }, { 8 } };
    return Arrays.asList(data);
  }
  
  public MultiThreadedTest(int i) {
    this.i = i;
  }

  @Test
  public void test() throws InterruptedException {
    Thread t = new Thread(aTest());
    t.start();
    t.join();
  }
  
  
  
  private Runnable aTest() {
    return new Runnable() {
      public void run() {
        CoveredByMultipleThreads testee  = new CoveredByMultipleThreads();
        testee.lotsOfLinesOfCode(i);
      }
      
    };
  };

}
