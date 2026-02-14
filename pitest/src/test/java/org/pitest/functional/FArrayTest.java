package org.pitest.functional;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.junit.Test;
import org.pitest.functional.predicate.False;
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
    assertThat(FArray.filter(this.is, i -> true)).isEqualTo(expected);
  }

  @Test
  public void shouldReturnEmptyListWhenFilteredOnFalse() {
    final List<Integer> expected = Collections.emptyList();
    assertThat(FArray.filter(this.is, False.instance())).isEqualTo(expected);
  }

  @Test
  public void shouldReturnOnlyMatchesToPredicate() {
    final Predicate<Integer> p = a -> a <= 2;
    final List<Integer> expected = Arrays.asList(1, 2);
    assertThat(FArray.filter(this.is, p)).isEqualTo(expected);
  }

  @Test
  public void shouldReturnEmptyListWhenGivenNull() {
    assertThat(FArray.filter(null,  i -> true)).isEmpty();
  }

  @Test
  public void shouldApplyFlatMapToAllItems() {
    final Function<Integer, Collection<Integer>> f = a -> Arrays.asList(a, a);
    final Collection<Integer> expected = Arrays.asList(1, 1, 2, 2, 3, 3, 4, 4,
        5, 5);
    assertThat(FArray.flatMap(this.is, f)).isEqualTo(expected);
  }

  @Test
  public void flatMapShouldTreatNullAsEmptyIterable() {
    assertThat(FArray.flatMap(null, objectToObjectIterable())).isEmpty();
  }

  private Function<Object, Iterable<Object>> objectToObjectIterable() {
    return a -> Collections.emptyList();
  }

  @Test
  public void containsShouldReturnFalseWhenPredicateNotMet() {
    final Integer[] xs = { 1, 2, 3 };
    assertThat(FArray.contains(xs, False.instance())).isFalse();
  }

  @Test
  public void containsShouldReturnTrueWhenPredicateMet() {
    final Integer[] xs = { 1, 2, 3 };
    assertThat(FArray.contains(xs,  i -> true)).isTrue();
  }

  @Test
  public void containsShouldStopProcessingOnFirstMatch() {
    final Integer[] xs = { 1, 2, 3 };
    final Predicate<Integer> predicate = a -> {
      if (a == 2) {
        throw new PitError("Did not shortcut");
      }
      return a == 1;
    };
    FArray.contains(xs, predicate);
    // pass
  }

}
