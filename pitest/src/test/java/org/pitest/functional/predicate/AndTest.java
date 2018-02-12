package org.pitest.functional.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.functional.prelude.Prelude.and;

import org.junit.Test;

public class AndTest {

  @Test
  public void shouldReturnFalseWhenSuppliedNoPredicate() {
    final And<Object> testee = and();
    assertFalse(testee.test(null));
  }

  @Test
  public void shouldBeTrueWhenGivenTrue() {
    final And<Object> testee = and(i -> true);
    assertTrue(testee.test(null));
  }

  @Test
  public void shouldBeFalseWhenGivenFalse() {
    final And<Object> testee = and(False.instance());
    assertFalse(testee.test(null));
  }

  @Test
  public void shouldBeTrueWhenGivenTrueAndTrue() {
    final And<Object> testee = and(i -> true, i -> true);
    assertTrue(testee.test(null));
  }

  @Test
  public void shouldBeFalseWhenGivenTrueAndFalse() {
    final And<Object> testee = and( i -> true, i -> false);
    assertFalse(testee.test(null));
  }

  @Test
  public void shouldBeFalseWhenGivenFalseAndFalse() {
    final And<Object> testee = and(False.instance(), False.instance());
    assertFalse(testee.test(null));
  }

}
