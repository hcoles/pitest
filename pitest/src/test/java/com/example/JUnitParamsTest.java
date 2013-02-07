package com.example;

import static junitparams.JUnitParamsRunner.$;
import junitparams.JUnitParamsRunner;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class JUnitParamsTest {

  @Test
  @junitparams.Parameters(method = "getTestInputValues")
  public void shouldGenerateCoverageForJUnitParams(final int input,
      final int expectedResult) {

  }

  public Object[] getTestInputValues() {
    return $($(1, 1), $(2, 2), $(3, 3));
  }
}