/**
 *
 */
package org.pitest.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.functional.predicate.False;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.predicate.True;
import org.pitest.util.PitError;

/**
 * @author henry
 *
 */
public class FArrayTest {

  final Integer[] is = { 1, 2, 3, 4, 5 };

  @Test
  public void shouldReturnAllEntriesWhenFilteredOnTrue() {
    final List<Integer> expected = Arrays.asList(this.is);
    assertEquals(expected, FArray.filter(this.is, True.all()));
  }

  @Test
  public void shouldReturnEmptyListWhenFilteredOnFalse() {
    final List<Integer> expected = Collections.emptyList();
    assertEquals(expected, FArray.filter(this.is, False.instance()));
  }

  @Test
  public void shouldReturnOnlyMatchesToPredicate() {
    final Predicate<Integer> p = new Predicate<Integer>() {
      @Override
      public Boolean apply(final Integer a) {
        return a <= 2;
      }
    };
    final List<Integer> expected = Arrays.asList(1, 2);
    assertEquals(expected, FArray.filter(this.is, p));
  }

  @Test
  public void shouldReturnEmptyListWhenGivenNull() {
    assertEquals(Collections.emptyList(), FArray.filter(null, True.all()));
  }

  @Test
  public void shouldApplyFlatMapToAllItems() {
    final F<Integer, Collection<Integer>> f = new F<Integer, Collection<Integer>>() {
      @Override
      public List<Integer> apply(final Integer a) {
        return Arrays.asList(a, a);
      }
    };
    final Collection<Integer> expected = Arrays.asList(1, 1, 2, 2, 3, 3, 4, 4,
        5, 5);
    assertEquals(expected, FArray.flatMap(this.is, f));
  }

  @Test
  public void flatMapShouldTreatNullAsEmptyIterable() {
    assertEquals(Collections.emptyList(),
        FArray.flatMap(null, objectToObjectIterable()));
  }

  private F<Object, Option<Object>> objectToObjectIterable() {
    return new F<Object, Option<Object>>() {
      @Override
      public Option<Object> apply(final Object a) {
        return Option.some(a);
      }

    };
  }

  @Test
  public void containsShouldReturnFalseWhenPredicateNotMet() {
    final Integer[] xs = { 1, 2, 3 };
    assertFalse(FArray.contains(xs, False.instance()));
  }

  @Test
  public void containsShouldReturnTrueWhenPredicateMet() {
    final Integer[] xs = { 1, 2, 3 };
    assertTrue(FArray.contains(xs, True.all()));
  }

  @Test
  public void containsShouldStopProcessingOnFirstMatch() {
    final Integer[] xs = { 1, 2, 3 };
    final Predicate<Integer> predicate = new Predicate<Integer>() {

      @Override
      public Boolean apply(final Integer a) {
        if (a == 2) {
          throw new PitError("Did not shortcut");
        }
        return a == 1;
      }

    };
    FArray.contains(xs, predicate);
    // pass
  }

}
