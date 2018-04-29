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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.pitest.testapi.TestGroupConfig.emptyConfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jmock.MockObjectTestCase;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.rules.ExternalResource;
import org.junit.rules.TestRule;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.mockito.MockitoAnnotations;
import org.pitest.junit.RunnerSuiteFinderTest.ThrowsOnDiscoverySuite;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestUnit;

import com.example.JUnitParamsTest;
import com.example.TheoryTest;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JUnitCustomRunnerTestUnitFinderTest {

  private JUnitCustomRunnerTestUnitFinder testee;

  @Before
  public void setup() {
    MockitoAnnotations.initMocks(this);
    this.testee = new JUnitCustomRunnerTestUnitFinder(new TestGroupConfig(), Collections.<String>emptyList(),
            Collections.<String>emptyList());
  }

  @Test
  public void shouldFindTheoryTestViaMethodNameTest() {
    final List<String> includedMethods = new ArrayList<>();
    includedMethods.add("testTheory1");
    includedMethods.add("testTheory3");
    setIncludedTestMethods(includedMethods);
    final Collection<TestUnit> actual = findWithTestee(TheoryTest.class);
    assertEquals(2, actual.size());
  }

  @Test
  public void shouldFindTestsInJUnitTheoryTest() {
    final Collection<TestUnit> actual = findWithTestee(TheoryTest.class);
    assertEquals(3, actual.size());
  }

  @RunWith(Parameterized.class)
  public static class ParameterisedTest {

    public ParameterisedTest(final int i) {

    }

    @Parameters
    public static Collection<Object[]> params() {
      return Arrays.asList(new Object[][] { { 1 }, { 2 } });
    }

    @Test
    public void test() {
    }

  }

  @Test
  public void shouldNotFindTestInParameterisedTestClass() {
    final Collection<TestUnit> actual = findWithTestee(ParameterisedTest.class);
    assertEquals(0, actual.size());
  }

  public static class CustomSuiteRunner extends Suite {

    public CustomSuiteRunner(final Class<?> klass, final RunnerBuilder rb)
        throws InitializationError {
      super(klass, rb);
    }

  }

  public static class One {
    @Test
    public void one() {

    }

    @Test
    public void two() {

    }
  }

  public static class Two {
    @Test
    public void one() {

    }

    @Test
    public void two() {

    }
  }

  @RunWith(CustomSuiteRunner.class)
  @SuiteClasses({ One.class, Two.class })
  public static class CustomSuite {

  }

  @Test
  public void shouldNotFindTestsInCustomSuite() {
    final Collection<TestUnit> actual = findWithTestee(CustomSuite.class);
    assertTrue(actual.isEmpty());
  }

  public static class Three {
    @Test
    public void one() {

    }

    @Test
    public void two() {

    }
  }

  @RunWith(CustomSuiteRunner.class)
  @SuiteClasses({ CustomSuite.class, Three.class })
  public static class NestCustomSuite {

  }

  @Test
  public void shouldNotFindTestsInNestedCustomSuites() {
    final Collection<TestUnit> actual = findWithTestee(NestCustomSuite.class);
    assertTrue(actual.isEmpty());
  }

  private Collection<TestUnit> findWithTestee(final Class<?> clazz) {
    return this.testee.findTestUnits(clazz);
  }

  public static class NotATest {

  }

  @Test
  public void shouldNotFindTestInNonTestClasses() {
    final Collection<TestUnit> actual = findWithTestee(NotATest.class);
    assertTrue(actual.isEmpty());
  }

  public static class JMockTest extends MockObjectTestCase {
    public void testExample() {

    }
  }

  @Test
  public void shouldFindTestUnitsInCustomJUnit3Class() {
    final Collection<TestUnit> actual = this.testee
        .findTestUnits(JMockTest.class);
    assertFalse(actual.isEmpty());
  }

  public static class JUnit3Test extends TestCase {
    public void testStuff() {

    }
  }

  public static class HasBeforeClassAnnotation {

    @BeforeClass
    public static void before() {

    }

    @Test
    public void testOne() {

    }

    @Test
    public void testTwo() {

    }

  }

  @Test
  public void shouldCreateSingleAtomicUnitWhenClassAnnotatedWithBeforeClass() {
    final Collection<TestUnit> actual = findWithTestee(HasBeforeClassAnnotation.class);
    assertEquals(1, actual.size());
  }

  public static class HasAfterClassAnnotation {

    @AfterClass
    public static void after() {

    }

    @Test
    public void testOne() {

    }

    @Test
    public void testTwo() {

    }

  }

  @Test
  public void shouldCreateSingleAtomicUnitWhenClassAnnotatedWithAfterClass() {
    final Collection<TestUnit> actual = findWithTestee(HasAfterClassAnnotation.class);
    assertEquals(1, actual.size());
  }

  public static class ClassRuleMethod {

    @ClassRule
    public static TestRule rule() {
      return new ExternalResource() {
      };
    }

    @Test
    public void testOne() {
    }

    @Test
    public void testTwo() {
    }
  }

  @Test
  public void shouldCreateSingleAtomicUnitWhenAnyMethodAnnotatedWithClassRule()
      throws Exception {
    final Collection<TestUnit> actual = findWithTestee(ClassRuleMethod.class);
    assertEquals(1, actual.size());
  }

  public static class ClassRuleField {

    @ClassRule
    public static TestRule rule = new ExternalResource() {
                                };

    @Test
    public void testOne() {
    }

    @Test
    public void testTwo() {
    }
  }

  @Test
  public void shouldCreateSingleAtomicUnitWhenAnyFieldAnnotatedWithClassRule()
      throws Exception {
    final Collection<TestUnit> actual = findWithTestee(ClassRuleMethod.class);
    assertEquals(1, actual.size());
  }

  public static class NoPublicConstructor extends TestCase {
    protected NoPublicConstructor() {

    }

    public void testFoo() {

    }
  }

  @Test
  public void shouldNotFindTestsInClassesExtendingTestCaseWithoutAPublicConstructor() {
    final Collection<TestUnit> actual = findWithTestee(NoPublicConstructor.class);
    assertEquals(0, actual.size());
  }

  public static class OwnSuiteMethod extends TestCase {

    public static TestSuite suite() {
      return new TestSuite(OwnSuiteMethod.class);
    }

    public void testOne() {

    }

    public void testTwo() {

    }

  }

  @Test
  public void shouldFindTestsInClassWithASuiteMethodThatReturnsItself() {
    final Collection<TestUnit> actual = findWithTestee(OwnSuiteMethod.class);
    assertEquals(2, actual.size());
  }

  public static class SuiteMethod extends TestCase {

    public static TestSuite suite() {
      return new TestSuite(JUnit3Test.class);
    }

  }

  @Test
  public void shouldNotFindTestsInClassWithASuiteMethodThatReturnsOthersClasses() {
    final Collection<TestUnit> actual = findWithTestee(SuiteMethod.class);
    assertEquals(0, actual.size());
  }

  @Test
  public void willFindSingleTestUnitInJUnitParamsTest() {
    final Collection<TestUnit> actual = findWithTestee(JUnitParamsTest.class);
    assertEquals(1, actual.size());
  }

  @Test
  public void shouldNotHaltWhenRunnerThrowsRuntimeException() {
    try {
      findWithTestee(ThrowsOnDiscoverySuite.class);
      // pass
    } catch(final RuntimeException ex) {
      fail();
    }
  }

  @Test
  public void includesSuppliedCategories() {
    setConfig(emptyConfig()
        .withIncludedGroups(ACategory.class.getName()));
    final Collection<TestUnit> actual = findWithTestee(Tagged.class);
    assertThat(actual).hasSize(1);
  }

  @Test
  public void excludesSuppliedCategories() {
    setConfig(emptyConfig()
        .withIncludedGroups(ACategory.class.getName())
        .withExcludedGroups(AnotherCategory.class.getName()));
    final Collection<TestUnit> actual = findWithTestee(Tagged.class);
    assertThat(actual).isEmpty();
  }

  @Test
  public void excludesInheritedCategories() {
    setConfig(emptyConfig()
        .withIncludedGroups(ACategory.class.getName())
        .withExcludedGroups(AnotherCategory.class.getName()));
    final Collection<TestUnit> actual = findWithTestee(IndirectlyTagged.class);
    assertThat(actual).isEmpty();
  }

  @Test
  public void excludesRunnersWhenRequested() {
    excludeRunner(BlockJUnit4ClassRunner.class);
    final Collection<TestUnit> actual = findWithTestee(HasExplicitRunner.class);
    assertThat(actual).isEmpty();
  }

  static interface ACategory {

  }

  static interface AnotherCategory {

  }

  @Category({ACategory.class, AnotherCategory.class})
  public static class Tagged {
    @Test
    public void testTwo() {
    }
  }

  public static class IndirectlyTagged extends Tagged {
    @Test
    public void test() {
    }
  }


  @RunWith(BlockJUnit4ClassRunner.class)
  public static class HasExplicitRunner {
    @Test
    public void foo() {

    }
  }


  private void setConfig(TestGroupConfig config) {
    this.testee = new JUnitCustomRunnerTestUnitFinder(
        config, Collections.<String>emptyList(), Collections.<String>emptyList());
  }


  private void excludeRunner(Class<? extends Runner> class1) {
    final List<String> include = Collections.<String>emptyList();
    final List<String> exclude = Collections.<String>emptyList();
    this.testee = new JUnitCustomRunnerTestUnitFinder(
            new TestGroupConfig(include,exclude), Collections.singletonList(class1.getName()),
            Collections.<String>emptyList());
  }

  private void setIncludedTestMethods(Collection<String> includedTestMethods) {
    final List<String> include = Collections.<String>emptyList();
    final List<String> exclude = Collections.<String>emptyList();
    this.testee = new JUnitCustomRunnerTestUnitFinder(
            new TestGroupConfig(include,exclude), Collections.<String>emptyList(), includedTestMethods);
  }


}
