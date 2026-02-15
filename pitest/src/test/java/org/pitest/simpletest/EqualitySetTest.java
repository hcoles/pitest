package org.pitest.simpletest;

import static org.assertj.core.api.Assertions.assertThat;

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
    assertThat(actual).isEqualTo(expected);
  }

  @Test
  public void containsShouldReturnTrueWhenMemberPresent() {
    this.testee.add("one");
    assertThat(this.testee.contains("one")).isTrue();
  }

  @Test
  public void containsShouldReturnFalseWhenMemberNotPresent() {
    this.testee.add("one");
    assertThat(this.testee.contains("two")).isFalse();
  }

  @Test
  public void toCollectionShouldReturnAllMembers() {
    this.testee.add("one");
    this.testee.add("two");
    final List<String> expected = Arrays.asList("one", "two");
    assertThat(this.testee.toCollection()).isEqualTo(expected);
  }

  @Test
  public void shouldOnlyAddOneInstanceOfEachValue() {
    this.testee.add("one");
    this.testee.add("two");
    this.testee.add("one");
    this.testee.add("two");
    final List<String> expected = Arrays.asList("one", "two");
    assertThat(this.testee.toCollection()).isEqualTo(expected);
  }

  @Test
  public void shouldOnlyAddOneInstanceOfEachValueViaAddAll() {
    final List<String> expected = Arrays.asList("one", "two");
    this.testee.addAll(expected);
    this.testee.addAll(expected);
    assertThat(this.testee.toCollection()).isEqualTo(expected);
  }

  @Test
  public void isEmptyShouldReturnTrueWhenEmpty() {
    assertThat(this.testee.isEmpty()).isTrue();
  }

  @Test
  public void isEmptyShouldReturnFalseWhenNotEmpty() {
    this.testee.add("foo");
    assertThat(this.testee.isEmpty()).isFalse();
  }

}