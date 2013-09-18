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

import junit.framework.TestCase;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.Repository;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.internal.IsolationUtils;

public class JUnitTestClassIdentifierTest {

  private JUnitTestClassIdentifier testee;
  private Repository               classRepostory;

  @Before
  public void setUp() {
    this.testee = new JUnitTestClassIdentifier();
    this.classRepostory = new Repository(new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader()));
  }

  static class JUnit3 extends TestCase {

  }

  @Test
  public void shouldRecogniseJUnit3ClassesAsTests() {
    this.testee.isATestClass(find(TestCase.class));
  }

  static abstract class AbstractJUnit3 extends TestCase {

  }

  @Test
  public void shouldRecogniseAbstractJUnit3ClassesAsTests() {
    this.testee.isATestClass(find(AbstractJUnit3.class));
  }

  @RunWith(Suite.class)
  private static class HasRunWith {

  }

  @Test
  public void shouldRecogniseClassesWithRunWithAnnotationsAsTests() {
    this.testee.isATestClass(find(HasRunWith.class));
  }

  private static class HasTestAnnotation {
    @Test
    public void aTest() {

    }

    static class Nested {

    }
  }

  @Test
  public void shouldRecogniseClassesWithJUnit4AnnotationsAsTests() {
    this.testee.isATestClass(find(HasTestAnnotation.class));
  }

  @Test
  public void shouldRecogniseNestedClassesWithTestsAsTestsClasses() {
    this.testee.isATestClass(find(HasTestAnnotation.Nested.class));
  }

  private static class DescendentOfTest extends HasTestAnnotation {

  }

  @Test
  public void shouldRecogniseDecendentsOfTestsAsTestsClasses() {
    this.testee.isATestClass(find(DescendentOfTest.class));
  }

  private ClassInfo find(final Class<?> clazz) {
    return this.classRepostory.fetchClass(clazz).value();
  }

}
