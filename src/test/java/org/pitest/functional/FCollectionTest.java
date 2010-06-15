/**
 * 
 */
package org.pitest.functional;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
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
  public void testFilterWillReturnFullList() {
    final List<Integer> expected = this.is;
    assertEquals(expected, FCollection.filter(this.is, True.instance()));
  }

  @Test
  public void testFilterWillReturnEmptyList() {
    final List<Integer> expected = Collections.emptyList();
    assertEquals(expected, FCollection.filter(this.is, False.instance()));
  }

  @Test
  public void testFilterWillReturnPartialList() {
    final Predicate<Integer> p = new Predicate<Integer>() {
      public Boolean apply(final Integer a) {
        return a <= 2;
      }
    };
    final List<Integer> expected = Arrays.asList(1, 2);
    assertEquals(expected, FCollection.filter(this.is, p));
  }

  @Test
  public void testForEachAppliedToAllItems() {
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
  public void testMapAppliedToAllItems() {
    assertEquals(this.is, FCollection.map(this.is, Common.id()));
  }

  @Test
  public void testFlatMapAppliedToAllItems() {
    final F<Integer, Collection<Integer>> f = new F<Integer, Collection<Integer>>() {
      public List<Integer> apply(final Integer a) {
        return Arrays.asList(a, a);
      }
    };
    final Collection<Integer> expected = Arrays.asList(1, 1, 2, 2, 3, 3, 4, 4,
        5, 5);
    assertEquals(expected, FCollection.flatMap(this.is, f));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testFlatMapApplieSideEffectToAllItems() {
    final SideEffect1<Integer> effect = mock(SideEffect1.class);
    final F<Integer, Collection<Integer>> f = new F<Integer, Collection<Integer>>() {
      public List<Integer> apply(final Integer a) {
        return Arrays.asList(a, a);
      }
    };
    FCollection.flatMap(this.is, f, effect);

    verify(effect, times(10)).apply(anyInt());

  }

}
