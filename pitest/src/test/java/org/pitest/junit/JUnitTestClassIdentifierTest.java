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
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classinfo.Repository;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.util.IsolationUtils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JUnitTestClassIdentifierTest {

  private JUnitTestClassIdentifier testee;
  private Repository               classRepostory;
  private List<String>             excludedGroups = new ArrayList<String>();
  private List<String>             includedGroups = new ArrayList<String>();

  @Before
  public void setUp() {
    TestGroupConfig groupConfig = new TestGroupConfig(excludedGroups, includedGroups);
    this.testee = new JUnitTestClassIdentifier(groupConfig);
    this.classRepostory = new Repository(new ClassloaderByteArraySource(
        IsolationUtils.getContextClassLoader()));
  }

  @Test
  public void shouldNotRecogniseNonTestClassesAsTests() {
    assertFalse(this.testee.isATestClass(find(java.lang.String.class)));
  }

  static class JUnit3 extends TestCase {
  }

  @Test
  public void shouldRecogniseJUnit3ClassesAsTests() {
    assertTrue(this.testee.isATestClass(find(JUnit3.class)));
  }

  static abstract class AbstractJUnit3 extends TestCase {

  }

  @Test
  public void shouldRecogniseAbstractJUnit3ClassesAsTests() {
    assertTrue(this.testee.isATestClass(find(AbstractJUnit3.class)));
  }

  @RunWith(Suite.class)
  private static class HasRunWith {

  }

  @Test
  public void shouldRecogniseClassesWithRunWithAnnotationsAsTests() {
    assertTrue(this.testee.isATestClass(find(HasRunWith.class)));
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
    assertTrue(this.testee.isATestClass(find(HasTestAnnotation.class)));
  }

  @Test
  public void shouldRecogniseNestedClassesWithTestsAsTestsClasses() {
    assertTrue(this.testee.isATestClass(find(HasTestAnnotation.Nested.class)));
  }

  private static class DescendentOfTest extends HasTestAnnotation {

  }

  @Test
  public void shouldRecogniseDecendentsOfTestsAsTestsClasses() {
    assertTrue(this.testee.isATestClass(find(DescendentOfTest.class)));
  }

  private interface AlphaTests {}
  private interface BetaTests {}
  private interface GammaTests {}

  private class NoCategoryTest extends HasTestAnnotation {}

  @Category(AlphaTests.class)
  private class AlphaCategoryTest extends HasTestAnnotation {}

  @Category(BetaTests.class)
  private class BetaCategoryTest extends HasTestAnnotation {}

  @Category({BetaTests.class, GammaTests.class})
  private class TwoCategoryTest extends HasTestAnnotation {}

  @Test
  public void noIncludedCategoriesMeansIncludeEverything() {
    includedGroups.clear();
    assertTrue(this.testee.isIncluded(find(NoCategoryTest.class)));
    assertTrue(this.testee.isIncluded(find(AlphaCategoryTest.class)));
    assertTrue(this.testee.isIncluded(find(BetaCategoryTest.class)));
    assertTrue(this.testee.isIncluded(find(TwoCategoryTest.class)));
  }

  @Test
  public void shouldOnlyIncludeTestsInIncludedCategories() {
    includedGroups.add(BetaTests.class.getName());
    assertFalse(this.testee.isIncluded(find(NoCategoryTest.class)));
    assertFalse(this.testee.isIncluded(find(AlphaCategoryTest.class)));
    assertTrue(this.testee.isIncluded(find(BetaCategoryTest.class)));
    assertTrue(this.testee.isIncluded(find(TwoCategoryTest.class)));
  }

  @Test
  public void noExcludedCategoriesMeansExcludeNothing() {
    excludedGroups.clear();
    assertFalse(this.testee.isExcluded(find(NoCategoryTest.class)));
    assertFalse(this.testee.isExcluded(find(AlphaCategoryTest.class)));
    assertFalse(this.testee.isExcluded(find(BetaCategoryTest.class)));
    assertFalse(this.testee.isExcluded(find(TwoCategoryTest.class)));
  }

  @Test
  public void shouldOnlyExcludeTestsInExcludedCategories() {
    excludedGroups.add(BetaTests.class.getName());
    assertFalse(this.testee.isExcluded(find(NoCategoryTest.class)));
    assertFalse(this.testee.isExcluded(find(AlphaCategoryTest.class)));
    assertTrue(this.testee.isExcluded(find(BetaCategoryTest.class)));
    assertTrue(this.testee.isExcluded(find(TwoCategoryTest.class)));
  }

  private ClassInfo find(final Class<?> clazz) {
    return this.classRepostory.fetchClass(clazz).value();
  }

}
