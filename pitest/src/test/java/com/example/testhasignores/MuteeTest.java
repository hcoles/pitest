package com.example.testhasignores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Ignore;
import org.junit.Test;

public class MuteeTest {
  
  @Test
  public void test1() {
    // does nothing
  }
  
  @Ignore
  @Test
  public void test2() {
    fail("not yet supported");
  }
  
  @Test
  public void thisOneKills() {
    assertEquals(1, Mutee.returnOne());
  }

}
