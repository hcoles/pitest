package org.pitest.functional.predicate;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * @author henry
 *
 */
public class FalseTest {

  @Test
  public void shouldAlwaysBeFalse() {
    assertFalse(False.instance().apply(null));
  }

}
