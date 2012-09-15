package org.pitest.boot;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

public class AtomicBitSetTest {

  @Test
  public void shouldReportCorrectLength() {
    final AtomicBitSet testee = new AtomicBitSet(32);
    assertEquals(32, testee.length());
  }

  @Test
  public void shouldGetAndSetBitsWithoutOverlap() {
    final AtomicBitSet testee = new AtomicBitSet(1024);
    for (int i = 0; i < (1024 - 1); i += 2) {
      testee.set(i);
    }

    for (int i = 0; i < (1024 - 1); i++) {
      if ((i % 2) == 0) {
        assertTrue(testee.get(i));
      } else {
        assertFalse(testee.get(i));
      }
    }
  }
  
  @Test
  public void shouldBeThreadSafe() throws InterruptedException {
    final int size = 1024;
    AtomicBitSet expected = runNonCurrently(size);
    AtomicBitSet actual = runConcurrently(size);
    for ( int i = 0; i != size; i++ ) {
      assertEquals(expected.get(i), actual.get(i));
    }
    
  }
  
  private AtomicBitSet runConcurrently(int size) throws InterruptedException {
    final AtomicBitSet testee = new AtomicBitSet(size);
    ExecutorService executor =
        new ThreadPoolExecutor(
          100, 
          100, 
          60, 
          TimeUnit.SECONDS, 
          new ArrayBlockingQueue<Runnable>(10, true),
          new ThreadPoolExecutor.CallerRunsPolicy());
    
    for ( int i = 0; i != 20; i ++ ) {
      executor.submit(set(testee,i + 7));
    }
    executor.shutdown();
    executor.awaitTermination(60, TimeUnit.SECONDS);
    return testee;
  }

  private AtomicBitSet runNonCurrently(int size) {
    final AtomicBitSet testee = new AtomicBitSet(size);
    for ( int i = 0; i != 20; i ++ ) {
      set(testee,i + 7).run();
    }
    return testee;
  }

  private static Runnable set(final AtomicBitSet testee, final int x) {
    return new Runnable() {
      public void run() {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        for ( int i = 0; i < testee.length(); i+=x) {
          testee.set(i);
        }
      }
      
    };
  }

}
