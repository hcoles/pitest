package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import org.jmock.MockObjectTestCase;
import org.junit.After;
import org.junit.Before;
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
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.StaticConfiguration;
import org.pitest.extension.TestFilter;
import org.pitest.extension.TestListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.NullDiscoveryListener;
import org.pitest.extension.common.UnGroupedStrategy;
import org.pitest.functional.Option;
import org.pitest.junit.JUnitCompatibleConfiguration;

public class TestJUnitConfiguration {

  private final JUnitCompatibleConfiguration testee = new JUnitCompatibleConfiguration();
  private Pitest                             pitest;
  private Container                          container;

  @Mock
  private TestListener                       listener;
  private StaticConfiguration                staticConfig;

  @Before
  public void createTestee() {
    MockitoAnnotations.initMocks(this);
    this.container = new UnContainer();
    this.staticConfig = new DefaultStaticConfig();
    this.staticConfig.getTestListeners().add(this.listener);
    this.pitest = new Pitest(this.staticConfig, this.testee);
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

  public static class JUnit3TestWithSingleStringConstructor extends TestCase {

    private final String name;

    public JUnit3TestWithSingleStringConstructor(final String name) {
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

  };

  @Test
  public void shouldCallsSingleStringArgumentsConstructorWithTestName() {
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

  static class HideFromJunit2 {
    public static class JUnit4TestWithWrongExpectations {
      @Test(expected = FileNotFoundException.class)
      public void testOne() throws FileNotFoundException {
        throw new IndexOutOfBoundsException();
      }
    }
  }

  @Test
  public void shouldReportErrorForTestsThatThrowWrongException() {
    run(HideFromJunit2.JUnit4TestWithWrongExpectations.class);
    verify(this.listener).onTestError(any(TestResult.class));
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

  static abstract class HideFromJunit6 {
    public static class TestWithTimeout {

      @Test(timeout = 5)
      protected void testBar() {
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
  }

  @Test
  public void shouldTimeTestsOut() {
    run(HideFromJunit6.TestWithTimeout.class);
    verify(this.listener).onTestError(any(TestResult.class));
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

  }

  @Test
  @Ignore("broken")
  public void shouldCreateTestsForEachJUnitParameter() {
    run(ParameterisedTest.class);
    verify(this.listener, times(3)).onTestSuccess(any(TestResult.class));
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
    // failing theories are actually errors
    verify(this.listener, times(2)).onTestError(any(TestResult.class));
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

  // static abstract class HideFromJUnit10 {

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

  // }

  @Test
  public void shouldSplitTestInSuitesIntoSeperateUnitsWhenUsingNonStandardSuiteRunners() {
    final List<TestUnit> actual = Pitest.findTestUnitsForAllSuppliedClasses(
        this.testee, new NullDiscoveryListener(), new UnGroupedStrategy(),
        Option.<TestFilter> none(), CustomSuite.class);

    System.out.println(actual);

    assertEquals(4, actual.size());

  }

  private void run(final Class<?> clazz) {
    this.pitest.run(this.container, clazz);
  }

}
