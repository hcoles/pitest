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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.Repository;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.util.IsolationUtils;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestInfoTest {

  private Repository repository;

  @Before
  public void setUp() {
    this.repository = new Repository(new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader()));
  }

  @Test
  public void isATestShouldReturnTrueForJUnit3Tests() {
    class JU3Test extends TestCase {

    }
    assertTrue(TestInfo.isATest().test(fetchClass(JU3Test.class)));
  }

  @Test
  public void isATestShouldReturnTrueForJUnit3Suites() {
    class JU3Test extends TestSuite {

    }
    assertTrue(TestInfo.isATest().test(fetchClass(JU3Test.class)));
  }

  @Test
  public void isATestShouldReturnTrueForJUnit4Tests() {
    assertTrue(TestInfo.isATest().test(fetchClass(TestInfoTest.class)));
  }

  @Test
  public void isATestShouldReturnFalseForNonTests() {
    assertFalse(TestInfo.isATest().test(fetchClass(String.class)));
  }

  static class Nested {

  }

  @Test
  public void isWithinATestClassShouldReturnTrueForNestedClassesWithinATest() {
    assertTrue(TestInfo.isWithinATestClass(fetchClass(Nested.class)));
  }

  private ClassInfo fetchClass(final Class<?> clazz) {
    return this.repository.fetchClass(clazz).get();
  }

}
