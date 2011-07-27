/*
 * Copyright 2011 Henry Coles
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

import static org.junit.Assert.*;
import junit.framework.TestCase;

import org.junit.Test;


public class TestInfoTest {

  @Test
  public void checkJUnitVersionShouldNoThrowErrorIfVersionAbove4PointSix() {
    TestInfo.checkJUnitVersion();
    // pass
  }

  @Test
  public void isATestShouldReturnTrueForJUnit3Tests() {
    class JU3Test extends TestCase {

    }
    assertTrue(TestInfo.isATest().apply(JU3Test.class));
  }

  @Test
  public void isATestShouldReturnTrueForJUnit4Tests() {
    assertTrue(TestInfo.isATest().apply(TestInfoTest.class));
  }

  @Test
  public void isATestShouldReturnFalseForNonTests() {
    assertFalse(TestInfo.isATest().apply(String.class));
  }

  static class Nested {

  }

  @Test
  public void isWithinATestClassShouldReturnTrueForNestedClassesWithinATest() {
    assertTrue(TestInfo.isWithinATestClass().apply(Nested.class));
  }

}
