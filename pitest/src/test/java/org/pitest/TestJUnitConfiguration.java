package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jmock.MockObjectTestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.experimental.categories.Categories;
import org.junit.experimental.categories.Categories.IncludeCategory;
import org.junit.experimental.categories.Category;
import org.junit.experimental.theories.DataPoint;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.junit.runners.Suite.SuiteClasses;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.functional.Option;
import org.pitest.help.PitHelpError;
import org.pitest.junit.JUnitCompatibleConfiguration;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.execute.Container;
import org.pitest.testapi.execute.FindTestUnits;
import org.pitest.testapi.execute.Pitest;
import org.pitest.testapi.execute.containers.UnContainer;

import com.example.JUnitParamsTest;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class TestJUnitConfiguration {

  private final JUnitCompatibleConfiguration testee = new JUnitCompatibleConfiguration(
                                                        new TestGroupConfig(), 
                                                        Collections.<String>emptyList());
  private Pitest                             pitest;
  private Container                          container;

  @Mock
  private TestListener                       listener;

  @Before
  public void createTestee() {
    MockitoAnnotations.initMocks(this);
    this.container = new UnContainer();

    this.pitest = new Pitest(this.listener);
  }

  public static class SimpleJUnit4Test {
    @Test
    public void testOne() {

    }
  }

  @Test
  public void shouldFindJunit4Tests() {
    run(SimpleJUnit4Test.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  public static class JUnit3TestWithSingleStringConstructorAndJUnit4Annotations
  extends TestCase {

    private final String name;

    public JUnit3TestWithSingleStringConstructorAndJUnit4Annotations(
        final String name) {
      super(name);
      this.name = name;
    }

    @Test
    public void testOne() {
      assertEquals("testOne", this.name);
    }

    @Test
    public void testTwo() {
      assertEquals("testTwo", this.name);
    }

  }

  @Test
  public void shouldCallSingleStringArgumentsConstructorWithTestNameWithAnnotations() {
    run(JUnit3TestWithSingleStringConstructorAndJUnit4Annotations.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
  }

  public static class JUnit3TestWithSingleStringConstructor extends TestCase {

    private final String name;

    public JUnit3TestWithSingleStringConstructor(final String name) {
      super(name);
      this.name = name;
    }

    public void testOne() {
      assertEquals("testOne", this.name);
    }

    public void testTwo() {
      assertEquals("testTwo", this.name);
    }

  }

  @Test
  public void shouldCallSingleStringArgumentsConstructorWithTestName() {
    run(JUnit3TestWithSingleStringConstructor.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
  }

  static class HideFromJunit1 {
    public static class JUnit4TestWithUnmetExpectations {
      @Test(expected = FileNotFoundException.class)
      public void testOne() {

      }
    }
  }

  @Test
  public void shouldFailTestsThatDoNotThrowExpectedException() {
    run(HideFromJunit1.JUnit4TestWithUnmetExpectations.class);
    verify(this.listener).onTestFailure(any(TestResult.class));
  }

  public static class JUnit4TestWithExpectations {
    @Test(expected = FileNotFoundException.class)
    public void testOne() throws FileNotFoundException {
      throw new FileNotFoundException();
    }
  }

  @Test
  public void shouldPassTestsThatThrowExpectedException() {
    run(JUnit4TestWithExpectations.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  public static class SimpleJUnit3Test extends TestCase {
    public void testOne() {

    }
  }

  @Test
  public void testFindJUnit3Tests() {
    run(SimpleJUnit3Test.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  public static class MixedJunit3And4Test extends TestCase {
    @Test
    public void testOne() {

    }
  }

  @Test
  public void shouldRunOnlyOneTestWhenMatchesBothJunit3And4Criteria() {
    run(MixedJunit3And4Test.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  public static class BaseTestCaseWithTest extends TestCase {
    public void testFoo() {

    }

    @Test
    public void testBar() {

    }
  }

  public static class InheritedTest extends BaseTestCaseWithTest {

  }

  public static class OverridesTestInParent extends BaseTestCaseWithTest {
    @Override
    public void testFoo() {

    }
  }

  @Test
  public void shouldRunTestsInheritedFromParent() {
    run(InheritedTest.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
  }

  @Test
  public void testOverriddenTestsCalledOnlyOnce() {
    run(OverridesTestInParent.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
  }

  static abstract class HideFromJunit4 {

    public static class MixedJUnit3And4SetupAndTearDown extends TestCase {
      public static int count = 0;

      @Override
      @Before
      public void setUp() {
        count++;
      }

      @Override
      @After
      public void tearDown() {
        count = count + 10;
      }

      @Test
      public void testCallCount() {
        assertEquals(1, count);
      }
    }
  }

  @Test
  public void shouldCallBeforeAndAfterMethodsEligableForBothJUnit3And4OnlyOnce() {
    run(HideFromJunit4.MixedJUnit3And4SetupAndTearDown.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
    assertEquals(11, HideFromJunit4.MixedJUnit3And4SetupAndTearDown.count);
  }

  public static class TestWithTimeout {

    @Test(timeout = 5)
    public void testBar() {
      for (int i = 0; i != 10; i++) {
        try {
          Thread.sleep(1000);
        } catch (final InterruptedException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }
  }

  @Test
  public void shouldTimeTestsOut() {
    run(TestWithTimeout.class);
    verify(this.listener).onTestFailure(any(TestResult.class));
  }

  @RunWith(Parameterized.class)
  public static class ParameterisedTest {
    int i;

    public ParameterisedTest(final int i) {
      this.i = i;
    }

    @Parameters
    public static Collection<Object[]> params() {
      return Arrays.asList(new Object[][] { { 1 }, { 2 }, { 3 } });
    }

    @Test
    public void test() {
      System.out.println(this.i);
    }

    @Test
    public void test2() {
      System.out.println("> " + this.i);
    }

  }

  @Test
  public void shouldCreateTestForEachParameterOfParameterizedTest() {
    run(ParameterisedTest.class);
    verify(this.listener, times(6)).onTestSuccess(any(TestResult.class));
  }

  static abstract class HideFromJUnit8 {
    @RunWith(Theories.class)
    public static class TheoriesTest {

      @DataPoint
      public static int i = 1;

      @Theory
      public void testTheory(final int i) {
        assertEquals(1, i);
      }

      @Theory
      public void failingTheory(final int i) {
        fail();
      }

      @Theory
      public void errorTheory(final int i) {
        throw new NullPointerException();
      }

    }
  }

  @Test
  public void shouldRunTestsCreatedByCustomRunners() {
    run(HideFromJUnit8.TheoriesTest.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
    verify(this.listener, times(2)).onTestFailure(any(TestResult.class));
  }

  static abstract class HideFromJUnit9 {

    public static class JMockTest extends MockObjectTestCase {
      org.jmock.Mock mock;

      @Override
      public void setUp() {
        this.mock = mock(Runnable.class);
        this.mock.expects(once()).method("run");
      }

      public void testFails() {

      }

      public void testPasses() {
        final Runnable r = (Runnable) this.mock.proxy();
        r.run();
      }
    }

  }

  @Test
  public void shouldRunJMock1Tests() {
    run(HideFromJUnit9.JMockTest.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
    verify(this.listener).onTestFailure(any(TestResult.class));
  }

  public static interface Marker {
  }

  @RunWith(Categories.class)
  @IncludeCategory(Marker.class)
  @SuiteClasses({ MarkerTest1.class, MarkerTest2.class })
  public static class CustomSuite {

  }

  @Category(Marker.class)
  public static class MarkerTest1 {
    @Test
    public void one() {

    }

    @Test
    public void two() {

    }
  }

  @Category(Marker.class)
  public static class MarkerTest2 {
    @Test
    public void one() {

    }

    @Test
    public void two() {

    }
  }

  @Test
  public void shouldSplitTestInSuitesIntoSeperateUnitsWhenUsingNonStandardSuiteRunners() {
    final List<TestUnit> actual = find(CustomSuite.class);

    System.out.println(actual);

    assertEquals(4, actual.size());

  }

  @Ignore
  public static class AnnotatedAsIgnored {

    @Test
    public void ignoreMe() {

    }

    @Test
    public void ignoreMeToo() {

    }

  }

  @Test
  public void shouldSkipAllMethodsInClassAnnotatedWithIgnore() {
    run(AnnotatedAsIgnored.class);
    verify(this.listener, times(1)).onTestSkipped((any(TestResult.class)));
    verify(this.listener, never()).onTestStart(any(Description.class));
  }

  public static class HasMethodAnnotatedAsIgnored {

    @Test
    @Ignore
    public void ignoreMe() {

    }

    @Test
    @Ignore
    public void ignoreMeToo() {

    }

    @Test
    public void dontIgnoreMe() {

    }

  }

  @Test
  public void shouldSkipAllMethodsAnnotatedWithIgnore() {
    run(HasMethodAnnotatedAsIgnored.class);
    verify(this.listener, times(2)).onTestSkipped((any(TestResult.class)));
    verify(this.listener).onTestSuccess((any(TestResult.class)));
  }

  public static class HasMethodAnnotatedAsIgnoredAndBeforeClassAnnotation {

    @BeforeClass
    public static void foo() {

    }

    @Test
    @Ignore
    public void ignoreMe() {

    }

    @Test
    public void dontIgnoreMe() {

    }

  }

  @Test
  public void shouldNotSkipEnabledTestsInAClassWithBeforeClassAnotationAndAnIgnoredTest() {
    run(HasMethodAnnotatedAsIgnoredAndBeforeClassAnnotation.class);
    verify(this.listener).onTestSuccess((any(TestResult.class)));
  }

  @Test
  public void shouldNotReportAnErrorWhenCorrectJUnitVersionOnClasspath() {
    assertEquals(Option.<PitHelpError> none(), this.testee.verifyEnvironment());
  }

  public static class HasAssumptionFailure {
    @Test
    public void testWithFailedAssumption() {
      assumeTrue(false);
    }
  }

  @Test
  public void shouldTreatAssumptionFailuesAsSuccess() {
    // see http://junit.sourceforge.net/doc/ReleaseNotes4.4.html#assumptions
    run(HasAssumptionFailure.class);
    verify(this.listener).onTestSuccess((any(TestResult.class)));
  }

  public static class JUnit3Test extends TestCase {
    public void testSomething() {

    }

    public void testSomethingElse() {

    }
  }

  public static class JUnit3SuiteMethod extends TestCase {
    public JUnit3SuiteMethod(final String testName) {
      super(testName);
    }

    public static junit.framework.Test suite() {
      final TestSuite suite = new TestSuite();
      suite.addTest(new JUnit3Test());
      return suite;
    }

  }

  @Test
  public void shouldDetectTestInJUnitThreeSuiteMethods() {
    final List<TestUnit> actual = find(JUnit3SuiteMethod.class);
    assertEquals(2, actual.size());
  }

  public static class OwnSuiteMethod extends TestCase {

    public static TestSuite suite() {
      return new TestSuite(OwnSuiteMethod.class);
    }

    public void testOne() {

    }

  }

  @Test
  public void shouldFindTestsInClassWithASuiteMethod() {
    final List<TestUnit> actual = find(OwnSuiteMethod.class);
    assertEquals(1, actual.size());
  }

  public static class NoSuitableConstructor extends TestCase {
    public NoSuitableConstructor(final int i, final int j, final long l) {

    }

    public void testSomething() {

    }
  }

  @Test
  public void shouldNotFindTestsInJUnit3TestsWithoutASuitableConstructor() {
    final List<TestUnit> actual = find(NoSuitableConstructor.class);
    assertEquals(0, actual.size());
  }

  @Test
  // see http://code.google.com/p/junitparams/
  public void shouldFindTestInJUnitParamsTest() {
    run(JUnitParamsTest.class);
    verify(this.listener, times(3)).onTestSuccess(any(TestResult.class));
  }

  public static class HasOneMethodAnnotatedAsIgnored {

    @Test
    public void dontIgnoreMe() {

    }

    @Test
    @Ignore
    public void ignoreMe() {

    }

    @Test
    public void dontIgnoreMeEither() {

    }

  }

  @Test
  public void shouldRunOtherMethodsInAClassWithOneIgnoredTest() {
    run(HasOneMethodAnnotatedAsIgnored.class);
    verify(this.listener, times(2)).onTestSuccess((any(TestResult.class)));
  }

  private void run(final Class<?> clazz) {
    this.pitest.run(this.container, this.testee, clazz);
  }

  private List<TestUnit> find(Class<?> clazz) {
    FindTestUnits finder = new FindTestUnits(this.testee);
    return finder.findTestUnitsForAllSuppliedClasses(Arrays
        .<Class<?>> asList(clazz));
  }

}
