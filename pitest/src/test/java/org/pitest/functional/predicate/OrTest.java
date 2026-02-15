package org.pitest.functional.predicate;

import static org.pitest.functional.prelude.Prelude.or;

import org.junit.Test;

import static org.assertj.core.api.Assertions.*;

public class OrTest {

  @Test
  public void shouldBeFalseWhenGivenNoPredicates() {
    final Or<Object> testee = or();
    assertThat(testee.test(null)).isFalse();
  }

  @Test
  public void shouldBeTrueWhenGivenTrue() {
    final Or<Object> testee = or( i -> true);
    assertThat(testee.test(null)).isTrue();
  }

  @Test
  public void shouldBeFalseWhenGivenFalse() {
    final Or<Object> testee = or(False.instance());
    assertThat(testee.test(null)).isFalse();
  }

  @Test
  public void shouldBeTrueWhenTrueOrTrue() {
    final Or<Object> testee = or( i -> true,  i -> true);
    assertThat(testee.test(null)).isTrue();
  }

  @Test
  public void shouldBeTrueWhenTrueOrFalse() {
    final Or<Object> testee = or( i -> true, False.instance());
    assertThat(testee.test(null)).isTrue();
  }

  @Test
  public void shouldeFalseWhenFalseOrFalse() {
    final Or<Object> testee = or(False.instance(), False.instance());
    assertThat(testee.test(null)).isFalse();
  }

}
