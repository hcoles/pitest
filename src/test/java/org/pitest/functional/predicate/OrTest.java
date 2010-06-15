package org.pitest.functional.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class OrTest {

  @SuppressWarnings("unchecked")
  @Test
  public void testNoArgs() {
    final Or<Object> testee = Or.instance();
    assertFalse(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTrue() {
    final Or<Object> testee = Or.instance(True.instance());
    assertTrue(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFalse() {
    final Or<Object> testee = Or.instance(False.instance());
    assertFalse(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTrueAndTrue() {
    final Or<Object> testee = Or.instance(True.instance(), True.instance());
    assertTrue(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testTrueAndFalse() {
    final Or<Object> testee = Or.instance(True.instance(), False.instance());
    assertTrue(testee.apply(null));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFalseAndFalse() {
    final Or<Object> testee = Or.instance(False.instance(), False.instance());
    assertFalse(testee.apply(null));
  }

}
