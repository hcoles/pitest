package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.containers.UnContainer;
import org.pitest.extension.TestListener;
import org.pitest.junit.JUnitCompatibleConfiguration;

public class TestJUnitConfiguration {

  JUnitCompatibleConfiguration testee = new JUnitCompatibleConfiguration();
  Pitest                       pitest;

  @Mock
  private TestListener         listener;

  @Before
  public void createTestee() {
    MockitoAnnotations.initMocks(this);
    this.pitest = new Pitest(new UnContainer(), this.testee);
    this.pitest.addListener(this.listener);
  }

  public static class SimpleJUnit4Test {
    @Test
    public void testOne() {

    }
  }

  @Test
  public void testFindsJunit4Test() {
    this.pitest.run(SimpleJUnit4Test.class);
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
  public void testCallsSingleStringArgumentsConstructorWithTestName() {
    this.pitest.run(JUnit3TestWithSingleStringConstructor.class);
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
  public void testFailsTestsThatDoNotThrowExpectedException() {
    this.pitest.run(HideFromJunit1.JUnit4TestWithUnmetExpectations.class);
    verify(this.listener).onTestFailure(any(TestResult.class));
  }

  public static class JUnit4TestWithExpectations {
    @Test(expected = FileNotFoundException.class)
    public void testOne() throws FileNotFoundException {
      throw new FileNotFoundException();
    }
  }

  @Test
  public void testPassesTestsThatThrowExpectedException() {
    this.pitest.run(JUnit4TestWithExpectations.class);
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
  public void testReportsErrorForTestsThatThrowWrongException() {
    this.pitest.run(HideFromJunit2.JUnit4TestWithWrongExpectations.class);
    verify(this.listener).onTestError(any(TestResult.class));
  }

  public static class SimpleJUnit3Test extends TestCase {
    public void testOne() {

    }
  }

  @Test
  public void testFindJUnit3Tests() {
    this.pitest.run(SimpleJUnit3Test.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  public static class MixedJunit3And4Test extends TestCase {
    @Test
    public void testOne() {

    }
  }

  @Test
  public void testOnlyOneTestRunIfMatchesBothJunit3And4Criteria() {
    this.pitest.run(MixedJunit3And4Test.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  static abstract class HideFromJunit3 {
    public static class BaseTestCaseWithTest extends TestCase {
      public void testFoo() {

      }

      @Test
      protected void testBar() {

      }
    }

    public static class InheritedTest extends BaseTestCaseWithTest {

    }

    public static class OverridesTestInParent extends BaseTestCaseWithTest {
      @Override
      public void testFoo() {

      }
    }

  }

  @Test
  public void testRunsTestsInheritedFromParent() {
    this.pitest.run(HideFromJunit3.InheritedTest.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
  }

  @Test
  public void testOverriddenTestsCalledOnlyOnce() {
    this.pitest.run(HideFromJunit3.OverridesTestInParent.class);
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
  public void testBeforeAndAfterMethodsEligableForBothJUnit3And4CalledOnlyOnce() {
    this.pitest.run(HideFromJunit4.MixedJUnit3And4SetupAndTearDown.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
    assertEquals(11, HideFromJunit4.MixedJUnit3And4SetupAndTearDown.count);
  }

  static abstract class HideFromJunit5 {
    public static class Junit3TestWithBeforeAndAfterAnnotations extends
        TestCase {

      public static int count;

      @BeforeClass
      public static void beforeClass() {
        count++;
      }

      @Override
      public void setUp() {
        assertEquals(1, count);
        count++;
      }

      public void testOne() {
        assertEquals(2, count);
        count++;
      }

      @Override
      public void tearDown() {
        assertEquals(3, count);
        count++;
      }

      @AfterClass
      public static void after() {
        assertEquals(4, count);
        count++;
      }
    }
  }

  @Test
  public void testBeforeAndAfterMethodsCalledInCorrectOrder() {
    this.pitest
        .run(HideFromJunit5.Junit3TestWithBeforeAndAfterAnnotations.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
    assertEquals(5,
        HideFromJunit5.Junit3TestWithBeforeAndAfterAnnotations.count);
  }

  static abstract class HideFromJunit6 {
    public static class TestWithTimeout {

      @Test(timeout = 30)
      protected void testBar() throws InterruptedException {
        Thread.sleep(100);
      }
    }
  }

  @Test
  public void testTimesTestsOut() {
    this.pitest.run(HideFromJunit6.TestWithTimeout.class);
    verify(this.listener).onTestError(any(TestResult.class));
  }

  static abstract class HideFromJUnit7 {
    @RunWith(Parameterized.class)
    public static class ParameterisedTest {

      public ParameterisedTest(final int i) {

      }

      @Parameters
      public static Collection<Object[]> params() {
        return Arrays.asList(new Object[][] { { 1 }, { 2 }, { 3 } });
      }

      @Test
      public void test() {

      }

    }
  }

  @Test
  public void testCreatesTestsForEachJUnitParameter() {
    this.pitest.run(HideFromJUnit7.ParameterisedTest.class);
    verify(this.listener, times(3)).onTestSuccess(any(TestResult.class));
  }

}
