package org.pitest.functional.predicate;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * @author henry
 * 
 */
public class TrueTest {

  @Test
  public void testReturnsTrue() {
    assertTrue(True.instance().apply(null));
  }

}
