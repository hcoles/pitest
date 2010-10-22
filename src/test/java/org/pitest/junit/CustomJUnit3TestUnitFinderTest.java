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
package org.pitest.junit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import junit.framework.TestCase;

import org.jmock.MockObjectTestCase;
import org.junit.Before;
import org.junit.Test;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.internal.TestClass;

public class CustomJUnit3TestUnitFinderTest {

  CustomJUnit3TestUnitFinder testee;

  @Before
  public void setup() {
    this.testee = new CustomJUnit3TestUnitFinder();
  }

  public static class JMockTest extends MockObjectTestCase {
    public void testExample() {

    }
  }

  @Test
  public void testFindsTestUnitsInCustomJUnit3Class() {
    final Collection<TestUnit> actual = this.testee.findTestUnits(
        new TestClass(JMockTest.class), null, new NullDiscoveryListener());
    assertFalse(actual.isEmpty());
  }

  public static class JUnit3Test extends TestCase {
    public void testStuff() {

    }
  }

  @Test
  public void testDoesNotFindTestUnitsInNonCustomJUnit3Class() {
    final Collection<TestUnit> actual = this.testee.findTestUnits(
        new TestClass(JUnit3Test.class), null, new NullDiscoveryListener());
    assertTrue(actual.isEmpty());
  }

}
