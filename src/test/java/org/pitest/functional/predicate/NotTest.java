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
public class NotTest {

  @Test
  public void testInvertsTrue() {
    assertFalse(Not.instance(True.instance()).apply(null));
  }

  @Test
  public void testInvertsFalse() {
    assertTrue(Not.instance(False.instance()).apply(null));
  }
}
