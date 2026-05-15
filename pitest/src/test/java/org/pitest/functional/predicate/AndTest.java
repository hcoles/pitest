package org.pitest.functional.predicate;

import static org.pitest.functional.prelude.Prelude.and;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class AndTest {

  @Test
  public void shouldReturnFalseWhenSuppliedNoPredicate() {
    final And<Object> testee = and();
    assertThat(testee.test(null)).isFalse();
  }

  @Test
  public void shouldBeTrueWhenGivenTrue() {
    final And<Object> testee = and(i -> true);
    assertThat(testee.test(null)).isTrue();
  }

  @Test
  public void shouldBeFalseWhenGivenFalse() {
    final And<Object> testee = and(False.instance());
    assertThat(testee.test(null)).isFalse();
  }

  @Test
  public void shouldBeTrueWhenGivenTrueAndTrue() {
    final And<Object> testee = and(i -> true, i -> true);
    assertThat(testee.test(null)).isTrue();
  }

  @Test
  public void shouldBeFalseWhenGivenTrueAndFalse() {
    final And<Object> testee = and( i -> true, i -> false);
    assertThat(testee.test(null)).isFalse();
  }

  @Test
  public void shouldBeFalseWhenGivenFalseAndFalse() {
    final And<Object> testee = and(False.instance(), False.instance());
    assertThat(testee.test(null)).isFalse();
  }

}
