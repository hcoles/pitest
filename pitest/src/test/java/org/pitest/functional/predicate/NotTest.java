/**
 *
 */
package org.pitest.functional.predicate;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.functional.prelude.Prelude.not;

import org.junit.Test;

/**
 * @author henry
 *
 */
public class NotTest {

  @Test
  public void shouldInvertTrue() {
    assertFalse(not(True.all()).apply(null));
  }

  @Test
  public void shouldInvertFalse() {
    assertTrue(not(False.instance()).apply(null));
  }
}
