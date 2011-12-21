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
package org.pitest.testng;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.example.testng.AnnotatedAtClassLevel;
import com.example.testng.AnnotatedAtMethodLevel;

public class TestNGTestUnitFinderTest {

  @Test
  public void shouldFindSingleTestUnitInAnnotatedClass() {
    final TestNGTestUnitFinder testee = new TestNGTestUnitFinder();
    assertEquals(1, testee.findTestUnits(AnnotatedAtClassLevel.class).size());
  }

  @Test
  public void shouldFindSingleTestUnitInClassWithAnnotatedMethods() {
    final TestNGTestUnitFinder testee = new TestNGTestUnitFinder();
    assertEquals(1, testee.findTestUnits(AnnotatedAtMethodLevel.class).size());
  }

  @Test
  public void shouldFindNoTestUnitsInUnannotatedClasses() {
    final TestNGTestUnitFinder testee = new TestNGTestUnitFinder();
    assertEquals(0, testee.findTestUnits(String.class).size());
  }

}
