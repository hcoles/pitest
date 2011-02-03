/**
 * 
 */
package org.pitest;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.FileNotFoundException;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.pitest.containers.UnContainer;
import org.pitest.extension.Container;
import org.pitest.extension.TestListener;
import org.pitest.testutil.AfterAnnotationForTesting;
import org.pitest.testutil.AfterClassAnnotationForTest;
import org.pitest.testutil.BeforeAnnotationForTesting;
import org.pitest.testutil.BeforeClassAnnotationForTest;
import org.pitest.testutil.ConfigurationForTesting;
import org.pitest.testutil.IgnoreAnnotationForTesting;
import org.pitest.testutil.TestAnnotationForTesting;

/**
 * @author henry
 * 
 */

public class TestPitest {

  private Pitest              testee;
  private Container           container;

  @Mock
  private TestListener        listener;
  private DefaultStaticConfig staticConfig;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.container = new UnContainer();
    this.staticConfig = new DefaultStaticConfig();
    this.staticConfig.getTestListeners().add(this.listener);
    this.testee = new Pitest(this.staticConfig, new ConfigurationForTesting());
  }

  public static class PassingTest {
    @TestAnnotationForTesting
    public void shouldPass() {

    }
  };

  @Test
  public void shouldNotifyAllListenersOfRunStart() {
    final TestListener listener2 = Mockito.mock(TestListener.class);
    this.staticConfig.addTestListener(listener2);
    run(PassingTest.class);
    verify(this.listener).onRunStart();
    verify(listener2).onRunStart();
  }

  @Test
  public void shouldNotifyAllListenersOfTestStart() {
    final TestListener listener2 = Mockito.mock(TestListener.class);
    this.staticConfig.addTestListener(listener2);
    run(PassingTest.class);
    verify(this.listener).onTestStart(any(Description.class));
    verify(listener2).onTestStart(any(Description.class));
  }

  @Test
  public void shouldNotifyAllListenersOfTestSuccess() {
    final TestListener listener2 = Mockito.mock(TestListener.class);
    this.staticConfig.addTestListener(listener2);
    run(PassingTest.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
    verify(listener2).onTestSuccess(any(TestResult.class));
  }

  @Test
  public void shouldNotifyAllListenersOfRunEnd() {
    final TestListener listener2 = Mockito.mock(TestListener.class);
    this.staticConfig.addTestListener(listener2);
    run(PassingTest.class);
    verify(this.listener).onRunEnd();
    verify(listener2).onRunEnd();
  }

  @Test
  public void shouldReportsSuccessIfNoExceptionThrown() {
    run(PassingTest.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  public static class FailsAssertion {
    @TestAnnotationForTesting
    public void willFail() {
      throw new AssertionError();
    }
  };

  @Test
  public void shouldReportedAsFailureIfAssertionThrown() {
    run(FailsAssertion.class);
    verify(this.listener).onTestFailure(any(TestResult.class));
  }

  public static class ExpectedExceptionThrown {
    @TestAnnotationForTesting(expected = NullPointerException.class)
    public void expectToThrowNullPointer() {
      throw new NullPointerException();
    }

  };

  @Test
  public void shouldReportSuccessIfExpectedExceptionThrown() {
    run(ExpectedExceptionThrown.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));

  }

  public static class ExpectedExceptionNotThrown {
    @TestAnnotationForTesting(expected = NullPointerException.class)
    public void expectToThrowNullPointer() {

    }
  };

  @Test
  public void shouldReportFailureIfExpectedExceptionNotThrown() {
    run(ExpectedExceptionNotThrown.class);
    verify(this.listener).onTestFailure(any(TestResult.class));
  }

  public static class UnexpectedExceptionThrown {
    @TestAnnotationForTesting
    public void willFailWithError() throws Exception {
      throw new FileNotFoundException();
    }
  };

  @Test
  public void shouldReportErrorWhenExceptionThrownAndNoExpectationSet() {
    run(UnexpectedExceptionThrown.class);
    verify(this.listener).onTestError(any(TestResult.class));
  }

  public static class WrongExceptionThrown {
    @TestAnnotationForTesting(expected = NullPointerException.class)
    public void willFailWithError() throws Exception {
      throw new FileNotFoundException();
    }
  };

  @Test
  public void shouldReportErrorWhenThrownExceptionDifferentFromExpectedException() {
    run(WrongExceptionThrown.class);
    verify(this.listener).onTestError(any(TestResult.class));
  }

  public static class SubclassOfExpectedExceptionThrown {
    @TestAnnotationForTesting(expected = Exception.class)
    public void shouldPass() throws Exception {
      throw new FileNotFoundException();
    }
  };

  @Test
  public void shouldReportSuccessWhenSubclassOfExpectedExceptionThrown() {
    run(SubclassOfExpectedExceptionThrown.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  public static class StaticTestCase {
    @TestAnnotationForTesting
    public static void staticTestMethod() {
    }
  };

  @Test
  public void shouldBeAbleToCallStaticTestMethods() {
    run(StaticTestCase.class);
    verify(this.listener).onTestSuccess(any(TestResult.class));
  }

  public static class HasBeforeAndAfterClassMethods {
    static int beforeClassCallCount = 0;
    static int afterClassCallCount  = 0;

    @BeforeClassAnnotationForTest
    public static void beforeClass() {
      beforeClassCallCount++;
    }

    @TestAnnotationForTesting
    public void firstTest() {
      if ((beforeClassCallCount != 1) || (afterClassCallCount > 0)) {
        throw new AssertionError();
      }
    }

    @TestAnnotationForTesting
    public void secondTest() {
      if ((beforeClassCallCount != 1) || (afterClassCallCount > 0)) {
        throw new AssertionError();
      }
    }

    @AfterClassAnnotationForTest
    public static void afterClass() {
      afterClassCallCount++;
    }

  }

  @Test
  public void shouldCallBeforeAndAfterClassMethodsExactlyOnceBeforeAndAfterTestsAreRun() {
    run(HasBeforeAndAfterClassMethods.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    assertEquals(1, HasBeforeAndAfterClassMethods.beforeClassCallCount);
    assertEquals(1, HasBeforeAndAfterClassMethods.afterClassCallCount);
  }

  public static class HasBeforeMethod {

    int callCount = 0;

    @BeforeAnnotationForTesting
    public void incrementCount() {
      this.callCount++;
    }

    @TestAnnotationForTesting
    public void testOne() {
      assertEquals(1, this.callCount);
    }

    @TestAnnotationForTesting
    public void testTwo() {
      assertEquals(1, this.callCount);
    }

  };

  @Test
  public void shouldCallBeforeMethodsBeforeEachTest() {
    run(HasBeforeMethod.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
  }

  public static class HasAfterMethod {

    static int callCount;

    @AfterAnnotationForTesting
    public void incrementCount() {
      callCount++;
    }

    @TestAnnotationForTesting
    public void testOne() {
      assertEquals(0, callCount);
    }

    @TestAnnotationForTesting
    public void testTwo() {
      assertEquals(1, callCount);
    }

  };

  @Test
  public void shouldCallAfterMethodsAfterEachTest() {
    run(HasAfterMethod.class);
    verify(this.listener, times(2)).onTestSuccess(any(TestResult.class));
    assertEquals(2, HasAfterMethod.callCount);
  }

  @IgnoreAnnotationForTesting
  public static class AnnotatedAsIgnored {

    @TestAnnotationForTesting
    public void ignoreMe() {

    }

    @TestAnnotationForTesting
    public void ignoreMeToo() {

    }

  };

  @Test
  public void shouldSkipAllMethodsInClassAnnotatedWithIgnore() {
    run(AnnotatedAsIgnored.class);
    verify(this.listener, times(2)).onTestSkipped((any(TestResult.class)));
  }

  public static class HasMethodAnnotatedAsIgnored {

    @TestAnnotationForTesting
    @IgnoreAnnotationForTesting
    public void ignoreMe() {

    }

    @TestAnnotationForTesting
    @IgnoreAnnotationForTesting
    public void ignoreMeToo() {

    }

    @TestAnnotationForTesting
    public void dontIgnoreMe() {

    }

  };

  @Test
  public void shouldSkipAllMethodsAnnotatedWithIgnore() {
    run(HasMethodAnnotatedAsIgnored.class);
    verify(this.listener, times(2)).onTestSkipped((any(TestResult.class)));
    verify(this.listener).onTestSuccess((any(TestResult.class)));
  }

  public static class HasMethodAnnotatedAsIgnoredAndWillRunAsGroup extends
      HasMethodAnnotatedAsIgnored {

    @BeforeClassAnnotationForTest
    public static void before() {

    }

  };

  @Test
  public void shouldSkipMethodsWithinAnAtomicGroupThatAreAnnotatedWithIgnore() {
    run(HasMethodAnnotatedAsIgnoredAndWillRunAsGroup.class);
    verify(this.listener, times(2)).onTestSkipped((any(TestResult.class)));
    verify(this.listener).onTestSuccess((any(TestResult.class)));
  }

  private void run(final Class<?> clazz) {
    this.testee.run(this.container, clazz);
  }
}