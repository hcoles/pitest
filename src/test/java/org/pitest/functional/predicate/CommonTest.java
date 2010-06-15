/**
 * 
 */
package org.pitest.functional.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author henry
 * 
 */
public class CommonTest {

  @Test
  public void testIsNullReturnsTrueWhenNull() {
    assertTrue(Common.fIsNull().apply(null));
  }

  @Test
  public void testIsNullReturnsFalseWhenNotNull() {
    assertFalse(Common.fIsNull().apply(1));
  }

  @Test
  public void testIsNotNullReturnsFalseWhenNull() {
    assertFalse(Common.fIsNotNull().apply(null));
  }

  @Test
  public void testIsNotNullReturnsTrueWhenNotNull() {
    assertTrue(Common.fIsNotNull().apply(1));
  }

}
