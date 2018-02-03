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
package com.example;

import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import junit.framework.TestCase;

@RunWith(Theories.class)
public class TheoryTest extends TestCase {
  @DataPoint
  public static Integer i = 1;

  @Theory
  public void testTheory1(final Integer i) {
    assertEquals(1, i.intValue());
  }

  @Theory
  public void testTheory2(final Integer i) {

  }

  @Theory
  public void testTheory3(final Integer i) {

  }
}
