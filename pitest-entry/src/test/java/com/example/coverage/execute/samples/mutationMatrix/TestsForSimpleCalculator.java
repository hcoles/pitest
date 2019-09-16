package com.example.coverage.execute.samples.mutationMatrix;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TestsForSimpleCalculator {

  @Test
  public void testSum() {
    assertEquals(3, SimpleCalculator.sum(2, 1));
  }

  @Test
  public void testSumWithNegativeNumber() {
	  assertEquals(1, SimpleCalculator.sum(4, -3));
  }

  @Test
  public void pseudoTestSum() {
    SimpleCalculator.sum(2, 1);
  }

  @Test
  public void unknownErrorOnMutant() {
    if (SimpleCalculator.sum(2,1) != 3) {
      System.exit(13);
    }
  }

  @Test
  public void timeoutOnMutant() {
    if (SimpleCalculator.sum(2,1) != 3) {
//      System.exit(14);
      try {
        Thread.sleep(5000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
  }

}
