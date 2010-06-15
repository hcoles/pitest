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

import java.util.Iterator;

import org.junit.Test;

public class OptionTest {

  @Test
  public void testSomeOrNoneReturnsNoneWhenPassedNull() {
    assertEquals(Option.none(), Option.someOrNone(null));
  }

  @Test
  public void testSomeOrNoneDoesNotReturnNoneWhenPassedAValue() {
    assertFalse(Option.someOrNone("foo").equals(Option.none()));
  }

  @Test
  public void testEqualsItself() {
    assertEquals(Option.none(), Option.none());
  }

  @Test
  public void testSomesWithSameValuesAreEqual() {
    assertEquals(Option.someOrNone("foo"), Option.someOrNone("foo"));
  }

  @Test
  public void testSomesWithDifferentValuesAreNotEqual() {
    assertFalse(Option.someOrNone("foo").equals(Option.someOrNone("bar")));
  }

  @Test
  public void testHasSomeReturnsFalseForNone() {
    assertFalse(Option.none().hasSome());
  }

  @Test
  public void testHasSomeReturnsTrueForSome() {
    assertTrue(Option.someOrNone("foo").hasSome());
  }

  @Test
  public void testHasNoneReturnsFalseForSome() {
    assertFalse(Option.someOrNone("foo").hasNone());
  }

  @Test
  public void testHasNoneReturnsTrueForNone() {
    assertTrue(Option.none().hasNone());
  }

  @Test
  public void testValueReturnsValueForSome() {
    assertEquals("foo", Option.someOrNone("foo").value());
  }

  @Test(expected = Error.class)
  public void testValueThrowsErrorForNone() {
    Option.none().value();
  }

  @Test
  public void testCanIterateOverNoValuesForNone() {
    assertFalse(Option.none().iterator().hasNext());
  }

  @Test
  public void testCanIterateOverExactlyOneValueForSome() {
    final Option<String> testee = Option.someOrNone("foo");
    final Iterator<String> it = testee.iterator();
    assertEquals("foo", it.next());
    assertFalse(it.hasNext());
  }

  @Test
  public void testGetOrElseReturnsElseForNone() {
    assertEquals("else", Option.none().getOrElse("else"));
  }

  @Test
  public void testGetOrElseReturnsValueForSome() {
    assertEquals("foo", Option.someOrNone("foo").getOrElse("else"));
  }

}
