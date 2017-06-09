/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.functional;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.functional.prelude.Prelude.id;
import static org.pitest.functional.prelude.Prelude.isEqualTo;
import static org.pitest.functional.prelude.Prelude.isGreaterThan;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import nl.jqno.equalsverifier.EqualsVerifier;

import org.junit.Before;
import org.junit.Test;
import org.pitest.functional.predicate.False;
import org.pitest.functional.predicate.True;
import org.pitest.functional.prelude.Prelude;

public class MutableListTest {

  private MutableList<Integer> testee;

  @Before
  public void setUp() {
    this.testee = new MutableList<Integer>();
  }

  @Test
  public void containsShouldReturnFalseIfValueNotInList() {
    assertFalse(this.testee.contains(1));
  }

  @Test
  public void containsAllShouldReturnFalseIfAllValuesNotInList() {
    this.testee.add(1);
    assertFalse(this.testee.containsAll(Arrays.asList(1, 2)));
  }

  @Test
  public void shouldBeAbleToAddToList() {
    this.testee.add(1);
    assertTrue(this.testee.contains(1));
  }

  @Test
  public void shouldBeAbleToAddAllToList() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    assertTrue(this.testee.containsAll(Arrays.asList(1, 2, 3)));
  }

  @Test
  public void isEmptyShouldReturnTrueWhenListIsEmpty() {
    assertTrue(this.testee.isEmpty());
  }

  @Test
  public void isEmptyShouldReturnFalseWhenListIsNotEmpty() {
    this.testee.add(1);
    assertFalse(this.testee.isEmpty());
  }

  @Test
  public void clearShouldRemoveAllItemsFromList() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    this.testee.clear();
    assertTrue(this.testee.isEmpty());
  }

  @Test
  public void removeShouldRemoveItemFromListAndReturnTrueWhenItemPresent() {
    this.testee.add(1);
    assertTrue(this.testee.remove(Integer.valueOf(1)));
    assertFalse(this.testee.contains(1));
  }

  @Test
  public void removeShouldReturnFalseWhenItemNotPresent() {
    assertFalse(this.testee.remove(Integer.valueOf(1)));
  }

  @Test
  public void removeAllShouldRemoveItemsFromListAndReturnTrueWhenItemsPresent() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    assertTrue(this.testee.removeAll(Arrays.asList(1, 2, 3)));
    assertTrue(this.testee.isEmpty());
  }

  @Test
  public void removeAllShouldReturnFalseWhenItemsNotPresent() {
    assertFalse(this.testee.removeAll(Arrays.asList(Integer.valueOf(1))));
  }

  @Test
  public void retainAllShouldRetainOnlySuppliedValues() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    assertTrue(this.testee.retainAll(Arrays.asList(1, 2)));
    assertEquals(new MutableList<Integer>(1, 2), this.testee);
  }

  @Test
  public void shouldCorrectlyReportSize() {
    assertEquals(0, this.testee.size());
    this.testee.add(1);
    assertEquals(1, this.testee.size());
  }

  @Test
  public void shouldConvertToObjectArray() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    final Object[] expected = { 1, 2, 3 };
    assertArrayEquals(expected, this.testee.toArray());
  }

  @Test
  public void shouldConvertToTypedArray() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    final Integer[] expected = { 1, 2, 3 };
    assertArrayEquals(expected, this.testee.toArray(new Integer[1]));
  }

  @Test
  public void shouldApplyForEachToAllEntreis() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    final Collection<Integer> actual = new ArrayList<Integer>();
    this.testee.forEach(Prelude.accumulateTo(actual));
    assertEquals(actual, Arrays.asList(1, 2, 3));
  }

  @Test
  public void shouldApplyFilterToAllEntries() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    assertEquals(Arrays.asList(2, 3), this.testee.filter(isGreaterThan(1)));
  }

  @Test
  public void shouldApplyMapToAllEntries() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    assertEquals(Arrays.asList(1, 2, 3), this.testee.map(id(Integer.class)));
  }

  @Test
  public void shouldApplyFlatMapToAllEntries() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    assertEquals(new MutableList<Integer>(1, 2, 3),
        this.testee.flatMap(Prelude.asList(Integer.class)));
  }

  @Test
  public void shouldReturnTrueWhenContainsCalledAndPredicateMatches() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    assertTrue(this.testee.contains(isEqualTo(1)));
  }

  @Test
  public void shouldReturnFalseWhenContainsCalledAndPredicateDoesNotMatch() {
    assertFalse(this.testee.contains(isEqualTo(10)));
  }

  @Test
  public void shouldBeAbleToAddValueAtGivenIndex() {
    this.testee.add(1);
    this.testee.add(1, 2);
    assertEquals(Integer.valueOf(2), this.testee.get(1));
  }

  @Test
  public void shouldBeAbleToAddAllAtGivenIndex() {
    this.testee.addAll(Arrays.asList(1, 200));
    this.testee.addAll(1, Arrays.asList(2, 3));
    assertTrue(this.testee.containsAll(Arrays.asList(1, 2, 3)));
  }

  @Test
  public void indexOfShouldReturnIndexOfValue() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    assertEquals(1, this.testee.indexOf(2));
  }

  @Test
  public void lastIndexOfShouldReturnLastIndexOfValue() {
    this.testee.addAll(Arrays.asList(1, 1, 1));
    assertEquals(2, this.testee.lastIndexOf(1));
  }

  @Test
  public void shouldBeAbleToRemoveItemAtGivenIndex() {
    this.testee.addAll(Arrays.asList(2, 4, 6));
    this.testee.remove(1);
    assertTrue(this.testee.containsAll(Arrays.asList(2, 6)));
  }

  @Test
  public void shouldBeAbleToCreateSubList() {
    this.testee.addAll(Arrays.asList(2, 4, 6, 8));
    assertEquals(Arrays.asList(4, 6), this.testee.subList(1, 3));
  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(MutableList.class).verify();
  }
  
  @Test
  public void shouldImplementFindFirst() {
    this.testee.addAll(Arrays.asList(1, 2, 3));
    assertEquals(Option.some(1), testee.findFirst(True.<Integer>all()));
    assertEquals(Option.none(), testee.findFirst(False.<Integer>instance()));
  }
}
