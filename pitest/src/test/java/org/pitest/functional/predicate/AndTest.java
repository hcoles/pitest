package org.pitest.functional.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.functional.prelude.Prelude.and;

import org.junit.Test;

public class AndTest {

  @SuppressWarnings("unchecked")
  @Test
  public void shouldReturnFalseWhenSuppliedNoPredicate() {
    final And<Object> testee = and();
    assertFalse(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldBeTrueWhenGivenTrue() {
    final And<Object> testee = and(True.all());
    assertTrue(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldBeFalseWhenGivenFalse() {
    final And<Object> testee = and(False.instance());
    assertFalse(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldBeTrueWhenGivenTrueAndTrue() {
    final And<Object> testee = and(True.all(), True.all());
    assertTrue(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldBeFalseWhenGivenTrueAndFalse() {
    final And<Object> testee = and(True.all(), False.instance());
    assertFalse(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void shouldBeFalseWhenGivenFalseAndFalse() {
    final And<Object> testee = and(False.instance(), False.instance());
    assertFalse(testee.apply(null));
  }

}
