package org.pitest.functional.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AndTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testNoArgs() {
    final And<Object> testee = And.instance();
    assertFalse(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTrue() {
    final And<Object> testee = And.instance(True.instance());
    assertTrue(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFalse() {
    final And<Object> testee = And.instance(False.instance());
    assertFalse(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTrueAndTrue() {
    final And<Object> testee = And.instance(True.instance(), True.instance());
    assertTrue(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTrueAndFalse() {
    final And<Object> testee = And.instance(True.instance(), False.instance());
    assertFalse(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFalseAndFalse() {
    final And<Object> testee = And.instance(False.instance(), False.instance());
    assertFalse(testee.apply(null));
  }

}
