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
import org.pitest.functional.Option;

public class FunctionsTest {

  @Test
  public void classToNameShouldReturnClassName() {
    assertEquals(String.class.getName(),
        Functions.classToName().apply(String.class));
  }

  @Test
  public void stringToClassShouldReturnClassWhenKnownToLoader() {
    assertEquals(Option.some(String.class),
        Functions.stringToClass().apply("java.lang.String"));
  }

  @Test
  public void stringToClassShouldReturnNoneWhenClassNotKnownToLoader() {
    assertEquals(Option.none(),
        Functions.stringToClass().apply("org.unknown.Unknown"));
  }

  @Test
  public void startWithShouldReturnTrueIfGivenStringStartsWithParameter() {
    assertTrue(Functions.startsWith("foo").apply("foobar"));
  }

  @Test
  public void startWithShouldReturnFalseIfGivenStringDoesNotStartsWithParameter() {
    assertFalse(Functions.startsWith("foo").apply("barfoo"));
  }

  @Test
  public void isInnerClassShouldReturnTrueForInnerClasses() {
    final Object o = new Object() {

    };

    assertTrue(Functions.isInnerClass().apply(o.getClass()));

  }

}
