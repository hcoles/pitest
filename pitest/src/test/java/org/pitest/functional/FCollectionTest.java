/**
 *
 */
package org.pitest.functional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.predicate.False;
import org.pitest.functional.prelude.Prelude;
import org.pitest.util.PitError;

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
    assertEquals(expected, FCollection.filter(this.is,  i -> true));
  }

  @Test
  public void shouldReturnEmptyListWhenFilteredOnFalse() {
    final List<Integer> expected = Collections.emptyList();
    assertEquals(expected, FCollection.filter(this.is, False.instance()));
  }

  @Test
  public void shouldReturnOnlyMatchesToPredicate() {
    final Predicate<Integer> p = a -> a <= 2;
    final List<Integer> expected = Arrays.asList(1, 2);
    assertEquals(expected, FCollection.filter(this.is, p));
  }

  @Test
  public void shouldApplyForEachToAllItems() {
    final List<Integer> actual = new ArrayList<>();
    final SideEffect1<Integer> e = a -> actual.add(a);

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
    final Function<Integer, Collection<Integer>> f = a -> Arrays.asList(a, a);
    final Collection<Integer> expected = Arrays.asList(1, 1, 2, 2, 3, 3, 4, 4,
        5, 5);
    assertEquals(expected, FCollection.flatMap(this.is, f));
  }

  @Test
  public void flatMapShouldTreatNullAsEmptyIterable() {
    assertEquals(Collections.emptyList(),
        FCollection.flatMap(null, objectToObjectIterable()));
  }

  private Function<Object, Iterable<Object>> objectToObjectIterable() {
    return a -> Collections.emptyList();
  }

  @Test
  public void containsShouldReturnFalseWhenPredicateNotMet() {
    final Collection<Integer> xs = Arrays.asList(1, 2, 3);
    assertFalse(FCollection.contains(xs, False.instance()));
  }

  @Test
  public void containsShouldReturnTrueWhenPredicateMet() {
    final Collection<Integer> xs = Arrays.asList(1, 2, 3);
    assertTrue(FCollection.contains(xs,  i -> true));
  }

  @Test
  public void containsShouldStopProcessingOnFirstMatch() {
    final Collection<Integer> xs = Arrays.asList(1, 2, 3);
    final Predicate<Integer> predicate = a -> {
      if (a == 2) {
        throw new PitError("Did not shortcut");
      }
      return a == 1;
    };
    FCollection.contains(xs, predicate);
    // pass
  }

  @Test
  public void foldShouldFoldValues() {
    final Collection<Integer> xs = Arrays.asList(1, 2, 3);
    final BiFunction<Integer, Integer, Integer> f = (a, b) -> a + b;

    final int actual = FCollection.fold(f, 2, xs);
    assertEquals(8, actual);
  }

  @Test
  public void flattenShouldReturnCollectionContainingAllSuppliedValues() {
    final Collection<Collection<Integer>> is = new ArrayList<>();
    is.add(Arrays.asList(1, 2, 3, 4, 5));
    is.add(Arrays.asList(6, 7, 8, 9));
    assertEquals(Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9),
        FCollection.flatten(is));

  }

  @Test
  public void shouldSplitCollectionIntoOneBucketWhenListSizeEqualToBucketSize() {
    this.is = Arrays.asList(1, 2, 3);
    final List<List<Integer>> actual = FCollection.splitToLength(3, this.is);
    assertEquals(1, actual.size());
    assertThat(actual.get(0)).contains(1, 2, 3);
  }

  @Test
  public void shouldSplitCollectionIntoTwoBucketsWhenListSizeOneGreaterThanBucketSize() {
    this.is = Arrays.asList(1, 2, 3);
    final List<List<Integer>> actual = FCollection.splitToLength(2, this.is);
    assertEquals(2, actual.size());
    assertThat(actual.get(0)).contains(1, 2);
    assertThat(actual.get(1)).contains(3);
  }

  @Test
  public void shouldSplitCollectionIntoManyBucketsWhenListManyTimesGreaterThanBucketSize() {
    this.is = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11);
    final List<List<Integer>> actual = FCollection.splitToLength(2, this.is);
    assertEquals(6, actual.size());
    assertThat(actual.get(0)).contains(1, 2);
    assertThat(actual.get(1)).contains(3, 4);
    assertThat(actual.get(5)).contains(11);
  }

  @Test
  public void shouldSplitIntoSingleBucketWhenAllItemsMeetsCriteria() {
    this.is = Arrays.asList(1, 2, 3);
    final Map<Integer, Collection<Integer>> actual = FCollection.bucket(
        this.is, fortyTwo());
    final Map<Integer, Collection<Integer>> expected = new HashMap<>();
    expected.put(42, Arrays.asList(1, 2, 3));
    assertEquals(expected, actual);
  }

  @Test
  public void shouldSplitIntoMultipleBuckets() {
    this.is = Arrays.asList(1, 2, 3);
    final Map<Integer, Collection<Integer>> actual = FCollection.bucket(
        this.is, Prelude.id(Integer.class));
    final Map<Integer, Collection<Integer>> expected = new HashMap<>();
    expected.put(1, Arrays.asList(1));
    expected.put(2, Arrays.asList(2));
    expected.put(3, Arrays.asList(3));
    assertEquals(expected, actual);
  }

  private Function<Integer, Integer> fortyTwo() {
    return a -> 42;
  }

}
