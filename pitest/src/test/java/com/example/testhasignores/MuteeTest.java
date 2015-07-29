package com.example.testhasignores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class MuteeTest {

  @BeforeClass
  public static void foo() {

  }

  @Test(expected = NullPointerException.class)
  public void test1() {
    // does nothing
    throw new NullPointerException();
  }

  @Ignore
  @Test
  public void test2() {
    fail("not yet supported");
  }

  @Test(expected = NullPointerException.class)
  public void thisOneKills() {
    assertEquals(1, Mutee.returnOne());
    throw new NullPointerException();
  }

}
