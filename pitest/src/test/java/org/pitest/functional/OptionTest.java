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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.functional.prelude.Prelude.id;
import static org.pitest.functional.prelude.Prelude.isEqualTo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;
import org.pitest.functional.Option.None;
import org.pitest.functional.Option.Some;
import org.pitest.functional.prelude.Prelude;

import nl.jqno.equalsverifier.EqualsVerifier;

public class OptionTest {

  private static final String FOO = "foo";

  @Test
  public void someShouldReturnNoneWhenPassedNull() {
    assertEquals(Option.none(), Option.some(null));
  }

  @Test
  public void someShouldNotReturnNoneWhenPassedAValue() {
    assertFalse(Option.some(FOO).equals(Option.none()));
  }

  @Test
  public void shouldEqualItself() {
    assertEquals(Option.none(), Option.none());
  }

  @Test
  public void shouldBeEqualsWhenConstructedWithSameValue() {
    assertEquals(Option.some(FOO), Option.some(FOO));
  }

  @Test
  public void shouldNotBeEqualWhenConstructedWithDifferentValuesAreNotEqual() {
    assertFalse(Option.some(FOO).equals(Option.some("bar")));
  }

  @Test
  public void noneShouldNotHaveSome() {
    assertFalse(Option.none().hasSome());
  }

  @Test
  public void someShouldHaveSome() {
    assertTrue(Option.some(FOO).hasSome());
  }

  @Test
  public void someShouldNotHaveNone() {
    assertFalse(Option.some(FOO).hasNone());
  }

  @Test
  public void noneShouldHaveNone() {
    assertTrue(Option.none().hasNone());
  }

  @Test
  public void shouldReturnUnderlyingValueWhenWeHaveSome() {
    assertEquals(FOO, Option.some(FOO).value());
  }

  @Test(expected = UnsupportedOperationException.class)
  public void shouldThrowErrorIfTryToRetrieveValueWhenWeHaveNone() {
    Option.none().value();
  }

  @Test
  public void shouldIterateOverNonValuesWhenNoneArePresent() {
    assertFalse(Option.none().iterator().hasNext());
  }

  @Test
  public void shouldIterateOverExactlyOneValueWhenOneIsPresent() {
    final Option<String> testee = Option.some(FOO);
    final Iterator<String> it = testee.iterator();
    assertEquals(FOO, it.next());
    assertFalse(it.hasNext());
  }

  @Test
  public void getOrElseShouldReturnElseConditionWhenNonePresent() {
    assertEquals("else", Option.none().getOrElse("else"));
  }

  @Test
  public void getOrElseShouldReturnValueWhenSomePresent() {
    assertEquals(FOO, Option.some(FOO).getOrElse("else"));
  }

  @Test
  public void forEachShouldBeAppliedToAllValues() {
    final Collection<String> actual = new ArrayList<String>();
    Option.some(FOO).forEach(Prelude.accumulateTo(actual));
    assertEquals(fooList(), actual);
  }

  @Test
  public void shouldMapSuppliedValue() {
    assertEquals(fooList(), Option.some(FOO).map(id(String.class)));
  }

  private List<String> fooList() {
    return Arrays.asList(FOO);
  }

  @Test
  public void shouldFlatMapSuppliedValue() {
    assertEquals(fooList(),
        Option.some(FOO).flatMap(Prelude.asList(String.class)));
  }

  @Test
  public void shouldFilterSuppliedValue() {
    assertEquals(fooList(), Option.some(FOO).filter(isEqualTo(FOO)));
  }

  @Test
  public void shouldImplementContains() {
    assertTrue(Option.some(FOO).contains(isEqualTo(FOO)));
  }

  @Test
  public void shouldObeyHashcodeEqualsContract() {
    EqualsVerifier.forClass(Some.class).verify();
    EqualsVerifier.forClass(None.class).verify();
  }

}
