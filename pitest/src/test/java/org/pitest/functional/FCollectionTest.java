/**
 * 
 */
package org.pitest.functional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.pitest.PitError;
import org.pitest.functional.predicate.False;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.predicate.True;

/**
 * @author henry
 * 
 */
public class FCollectionTest {

  private List<Integer> is;

  @Before
  public void setUp() {
    this.is = Arrays.asList(1, 2, 3, 4, 5);
  }

  @Test
  public void shouldReturnsAllEntriesWhenFilteredOnTrue() {
    final List<Integer> expected = this.is;
    assertEquals(expected, FCollection.filter(this.is, True.all()));
  }

  @Test
  public void shouldReturnEmptyListWhenFilteredOnFalse() {
    final List<Integer> expected = Collections.emptyList();
    assertEquals(expected, FCollection.filter(this.is, False.instance()));
  }

  @Test
  public void shouldReturnOnlyMatchesToPredicate() {
    final Predicate<Integer> p = new Predicate<Integer>() {
      public Boolean apply(final Integer a) {
        return a <= 2;
      }
    };
    final List<Integer> expected = Arrays.asList(1, 2);
    assertEquals(expected, FCollection.filter(this.is, p));
  }

  @Test
  public void shouldApplyForEachToAllItems() {
    final List<Integer> actual = new ArrayList<Integer>();
    final SideEffect1<Integer> e = new SideEffect1<Integer>() {
      public void apply(final Integer a) {
        actual.add(a);
      }

    };

    FCollection.forEach(this.is, e);

    assertEquals(this.is, actual);
  }

  @Test
  public void shouldApplyMapToAllItems() {
    assertEquals(this.is, FCollection.map(this.is, Prelude.id()));
  }

  @Test
  public void mapShouldTreatNullAsAnEmptyIterable() {
    assertEquals(Collections.emptyList(), FCollection.map(null, Prelude.id()));
  }

  @Test
  public void shouldApplyFlatMapToAllItems() {
    final F<Integer, Collection<Integer>> f = new F<Integer, Collection<Integer>>() {
      public List<Integer> apply(final Integer a) {
        return Arrays.asList(a, a);
      }
    };
    final Collection<Integer> expected = Arrays.asList(1, 1, 2, 2, 3, 3, 4, 4,
        5, 5);
    assertEquals(expected, FCollection.flatMap(this.is, f));
  }

  @Test
  public void flatMapShouldTreatNullAsEmptyIterable() {
    assertEquals(Collections.emptyList(),
        FCollection.flatMap(null, objectToObjectIterable()));
  }

  private F<Object, Option<Object>> objectToObjectIterable() {
    return new F<Object, Option<Object>>() {
      public Option<Object> apply(final Object a) {
        return Option.some(a);
      }

    };
  }

  @Test
  public void containsShouldReturnFalseWhenPredicateNotMet() {
    final Collection<Integer> xs = Arrays.asList(1, 2, 3);
    assertFalse(FCollection.contains(xs, False.instance()));
  }

  @Test
  public void containsShouldReturnTrueWhenPredicateMet() {
    final Collection<Integer> xs = Arrays.asList(1, 2, 3);
    assertTrue(FCollection.contains(xs, True.all()));
  }

  @Test
  public void containsShouldStopProcessingOnFirstMatch() {
    final Collection<Integer> xs = Arrays.asList(1, 2, 3);
    final Predicate<Integer> predicate = new Predicate<Integer>() {

      public Boolean apply(final Integer a) {
        if (a == 2) {
          throw new PitError("Did not shortcut");
        }
        return a == 1;
      }

    };
    FCollection.contains(xs, predicate);
    // pass
  }

  @Test
  public void foldShouldFoldValues() {
    final Collection<Integer> xs = Arrays.asList(1, 2, 3);
    final F2<Integer, Integer, Integer> f = new F2<Integer, Integer, Integer>() {

      public Integer apply(final Integer a, final Integer b) {
        return a + b;
      }

    };

    final int actual = FCollection.fold(f, 2, xs);
    assertEquals(8, actual);
  }

}
