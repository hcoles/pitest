package com.example.mutatablecodeintest;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class MuteeTest {

  int i;

  public MuteeTest(final int i) {
    this.i = i;
  }

  @Parameters
  public static Collection<Object[]> params() {
    if (Mutee.returnOne() != 1) {
      throw new RuntimeException();
    }
    return Arrays.asList(new Object[][] { { 1 }, { 2 }, { 3 } });
  }

  @Test
  public void test1() {

  }

  @Test
  public void test2() {

  }
}
