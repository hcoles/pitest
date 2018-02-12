package org.pitest.simpletest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

public class EqualitySetTest {

  EqualitySet<String> testee;

  @Before
  public void createTestee() {
    final EqualityStrategy<String> e = (lhs, rhs) -> lhs.equals(rhs);
    this.testee = new EqualitySet<>(e);
  }

  @Test
  public void shouldIterateOverMembers() {
    this.testee.add("one");
    this.testee.add("two");
    final List<String> expected = Arrays.asList("one", "two");
    final List<String> actual = new ArrayList<>();
    for (final String each : this.testee) {
      actual.add(each);
    }
    assertEquals(expected, actual);
  }

  @Test
  public void containsShouldReturnTrueWhenMemberPresent() {
    this.testee.add("one");
    assertTrue(this.testee.contains("one"));
  }

  @Test
  public void containsShouldReturnFalseWhenMemberNotPresent() {
    this.testee.add("one");
    assertFalse(this.testee.contains("two"));
  }

  @Test
  public void toCollectionShouldReturnAllMembers() {
    this.testee.add("one");
    this.testee.add("two");
    final List<String> expected = Arrays.asList("one", "two");
    assertEquals(expected, this.testee.toCollection());
  }

  @Test
  public void shouldOnlyAddOneInstanceOfEachValue() {
    this.testee.add("one");
    this.testee.add("two");
    this.testee.add("one");
    this.testee.add("two");
    final List<String> expected = Arrays.asList("one", "two");
    assertEquals(expected, this.testee.toCollection());
  }

  @Test
  public void shouldOnlyAddOneInstanceOfEachValueViaAddAll() {
    final List<String> expected = Arrays.asList("one", "two");
    this.testee.addAll(expected);
    this.testee.addAll(expected);
    assertEquals(expected, this.testee.toCollection());
  }

  @Test
  public void isEmptyShouldReturnTrueWhenEmpty() {
    assertTrue(this.testee.isEmpty());
  }

  @Test
  public void isEmptyShouldReturnFalseWhenNotEmpty() {
    this.testee.add("foo");
    assertFalse(this.testee.isEmpty());
  }

}
