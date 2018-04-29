package com.example.java7;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

public class HasIfOnAStringEqualityTesteeTest {

  private final HasIfOnAStringEqualityTestee testee = new HasIfOnAStringEqualityTestee();

  @Test
  public void ifStringShouldReturnA() throws Exception {
    // given
    final String input = "a";

    // when
    final String result = testee.ifString(input);

    // then
    assertThat(result).isEqualTo("A");
  }

  @Test
  public void ifStringShouldReturnB() throws Exception {
    // given
    final String input = "b";

    // when
    final String result = testee.ifString(input);

    // then
    assertThat(result).isEqualTo("B");
  }

  @Test
  public void ifStringShouldReturnC() throws Exception {
    // given
    final String input = "c";

    // when
    final String result = testee.ifString(input);

    // then
    assertThat(result).isEqualTo("C");
  }

  @Test(expected = IllegalArgumentException.class)
  public void ifStringShouldThrowIllegalArgumentException() throws Exception {
    // given
    final String input = "x";

    // when
    testee.ifString(input);

    // then
    // exception
  }
}
