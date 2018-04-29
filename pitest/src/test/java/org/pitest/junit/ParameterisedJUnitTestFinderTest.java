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
package org.pitest.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.pitest.testapi.TestUnit;

public class ParameterisedJUnitTestFinderTest {

  private ParameterisedJUnitTestFinder testee;

  @Before
  public void setup() {
    this.testee = new ParameterisedJUnitTestFinder();
  }

  @RunWith(Parameterized.class)
  public static class ParameterisedTest {

    public ParameterisedTest(final int i) {

    }

    @Parameters
    public static Collection<Object[]> params() {
      return Arrays.asList(new Object[][] { { 1 }, { 2 }, { 3 }, { 4 } });
    }

    @Test
    public void test() {
    }

    @Test
    public void anotherTest() {

    }

  }

  @Test
  public void shouldCreateTestUnitForEachParameterMethodCombinationOfParameterizedTest() {
    final Collection<TestUnit> actual = findWithTestee(ParameterisedTest.class);
    assertEquals(8, actual.size());
  }

  @Test
  public void shouldReturnNoTestForNonParameterisedTest() {
    final Collection<TestUnit> actual = findWithTestee(ParameterisedJUnitTestFinderTest.class);
    assertTrue(actual.isEmpty());
  }

  private Collection<TestUnit> findWithTestee(final Class<?> clazz) {
    return this.testee.findTestUnits(clazz);
  }

}
