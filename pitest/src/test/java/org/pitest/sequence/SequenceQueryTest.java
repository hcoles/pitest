package org.pitest.sequence;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

public class SequenceQueryTest {

  @Test
  public void shouldMatchSingleLiterals() {
    SequenceMatcher<Integer> testee = QueryStart
        .match(eq(1))
        .compile();

    assertTrue(testee.matches(asList(1)));
    assertFalse(testee.matches( asList(2)));
  }

  @Test
  public void shouldMatchSimpleSequences() {
   SequenceMatcher<Integer> testee = QueryStart
       .match(eq(1))
       .then(eq(2))
       .then(eq(3))
       .compile();

    assertTrue(testee.matches(asList(1, 2, 3)));
    assertFalse(testee.matches(asList(1, 2)));
    assertFalse(testee.matches(asList(1, 2, 3, 4)));
  }

  @Test
  public void shouldMatchSimpleOrs() {
    SequenceQuery<Integer> right = QueryStart.match(eq(2));

   SequenceMatcher<Integer> testee = QueryStart
       .match(eq(1))
       .or(right)
       .compile();

    assertTrue(testee.matches(asList(1)));
    assertTrue(testee.matches(asList(2)));
    assertFalse(testee.matches(asList(3)));
  }

  @Test
  public void shouldMatchSimpleZeroOrMores() {
    SequenceQuery<Integer> right = QueryStart.match(eq(2));

   SequenceMatcher<Integer> testee = QueryStart
       .match(eq(1))
       .zeroOrMore(right)
       .compile();

    assertTrue(testee.matches(asList(1)));
    assertTrue(testee.matches(asList(1, 2)));
    assertTrue(testee.matches(asList(1, 2, 2, 2)));
    assertFalse(testee.matches(asList(1, 2, 3)));
    assertFalse(testee.matches(asList(1, 3)));
  }

  @Test
  public void shouldMatchSimpleOneOrMores() {
    SequenceQuery<Integer> right = QueryStart.match(eq(2));

   SequenceMatcher<Integer> testee = QueryStart
       .match(eq(1))
       .oneOrMore(right)
       .compile();

    assertFalse(testee.matches(asList(1)));
    assertTrue(testee.matches(asList(1, 2)));
    assertTrue(testee.matches(asList(1, 2, 2, 2)));
    assertFalse(testee.matches(asList(1, 2, 3)));
    assertFalse(testee.matches(asList(1, 3)));
  }

  @Test
  public void shouldMatchAnyOf() {
    SequenceQuery<Integer> left = QueryStart.match(eq(2));

    SequenceQuery<Integer> right = QueryStart.match(eq(3));

    SequenceMatcher<Integer> testee = QueryStart.match(eq(1))
        .thenAnyOf(left, right)
        .then(eq(99))
        .compile();

    assertTrue(testee.matches(asList(1, 2, 99)));
    assertTrue(testee.matches(asList(1, 3, 99)));
    assertFalse(testee.matches(asList(1, 2)));
    assertFalse(testee.matches(asList(1, 2, 3, 99)));
  }
  
  @Test
  public void shouldSkipItemsMatchingIgnoreList() {
    SequenceMatcher<Integer> testee = QueryStart
        .match(eq(1))
        .then(eq(2))
        .compile(QueryParams.params(Integer.class).withIgnores(eq(99)));
    
    assertTrue(testee.matches(asList(1, 99, 2)));
  }

  private Match<Integer> eq(final int i) {
    return Match.isEqual(i);
  }
  
  private static List<Integer> asList(Integer... is) {
    return Arrays.asList(is);
  }

}
