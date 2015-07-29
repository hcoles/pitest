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
package org.pitest.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FunctionsTest {

  @Test
  public void classToNameShouldReturnClassName() {
    assertEquals(String.class.getName(),
        Functions.classToName().apply(String.class));
  }

  @Test
  public void startWithShouldReturnTrueIfGivenStringStartsWithParameter() {
    assertTrue(Functions.startsWith("foo").apply("foobar"));
  }

  @Test
  public void startWithShouldReturnFalseIfGivenStringDoesNotStartsWithParameter() {
    assertFalse(Functions.startsWith("foo").apply("barfoo"));
  }

  static enum Foo {
    A, B, C, Bar;
  };

  @Test
  public void stringToEnumShouldConvertStringsToEnumValues() {
    assertEquals(Foo.A, Functions.stringToEnum(Foo.class).apply("A"));
    assertEquals(Foo.Bar, Functions.stringToEnum(Foo.class).apply("Bar"));
  }

  @Test
  public void classNameToJVMClassNameShouldConvertDotsToSlashes() {
    assertEquals("a/b/c", Functions.classNameToJVMClassName().apply("a.b.c"));
  }

  @Test
  public void jvmClassToClassNameShouldConvertSlashesToDots() {
    assertEquals("a.b.c", Functions.jvmClassToClassName().apply("a/b/c"));
  }

}
