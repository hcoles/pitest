/**
 * 
 */
package org.pitest.functional;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.functional.predicate.False;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.predicate.True;

/**
 * @author henry
 * 
 */
public class FArrayTest {

  final Integer[] is = { 1, 2, 3, 4, 5 };

  @Test
  public void testFilterWillReturnFullList() {
    final List<Integer> expected = Arrays.asList(this.is);
    assertEquals(expected, FArray.filter(this.is, True.instance()));
  }

  @Test
  public void testFilterWillReturnEmptyList() {
    final List<Integer> expected = Collections.emptyList();
    assertEquals(expected, FArray.filter(this.is, False.instance()));
  }

  @Test
  public void testFilterWillReturnPartialList() {
    final Predicate<Integer> p = new Predicate<Integer>() {
      public Boolean apply(final Integer a) {
        return a <= 2;
      }
    };
    final List<Integer> expected = Arrays.asList(1, 2);
    assertEquals(expected, FArray.filter(this.is, p));
  }

}
