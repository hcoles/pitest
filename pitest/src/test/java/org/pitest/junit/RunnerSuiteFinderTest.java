package org.pitest.junit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;

import com.example.TheoryTest;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RunnerSuiteFinderTest {

  private RunnerSuiteFinder testee;

  @Before
  public void setup() {
    this.testee = new RunnerSuiteFinder();
  }

  @Test
  public void shouldNotFindClassesInJUnitTheoryTest() {
    final Collection<Class<?>> actual = findWithTestee(TheoryTest.class);
    assertTrue(actual.isEmpty());
  }

  public static class CustomSuiteRunner extends Suite {

    public CustomSuiteRunner(final Class<?> klass, final RunnerBuilder rb)
        throws InitializationError {
      super(klass, rb);
    }

  }

  public static class One extends TestCase {
    public void testSomething() {
    }
  }

  public static class Two extends One {
  }

  @RunWith(CustomSuiteRunner.class)
  @SuiteClasses({ One.class, Two.class })
  static class CustomSuite {

  }

  @Test
  public void shouldFindSuiteClassesInCustomSuite() {
    final Collection<Class<?>> actual = findWithTestee(CustomSuite.class);
    final Collection<Class<?>> expected = Arrays.<Class<?>> asList(One.class,
        Two.class);

    assertContains(expected, actual);
  }

  public static class JUnit3Suite extends TestCase {
    public static junit.framework.Test suite() {

      final TestSuite suite = new TestSuite();
      suite.addTestSuite(One.class);
      suite.addTestSuite(Two.class);

      return suite;
    }
  }

  @Test
  public void shouldFindSuiteClassesInJUnit3Suite() {
    final Collection<Class<?>> actual = findWithTestee(JUnit3Suite.class);
    final Collection<Class<?>> expected = Arrays.<Class<?>> asList(One.class,
        Two.class);
    assertContains(expected, actual);
  }

  public static class JUnit3SuiteMethod extends TestCase {
    public JUnit3SuiteMethod(final String testName) {
      super(testName);
    }

    public static junit.framework.Test suite() {
      final TestSuite suite = new TestSuite();
      suite.addTest(new One());
      suite.addTest(new Two());
      return suite;
    }

  }

  @Test
  public void shouldFindSuiteClassesInJUnit3SuiteMethod() {
    final Collection<Class<?>> actual = findWithTestee(JUnit3SuiteMethod.class);
    final Collection<Class<?>> expected = Arrays.<Class<?>> asList(One.class,
        Two.class);
    assertContains(expected, actual);
  }

  @Test
  public void shouldFindSuiteClasseInNestedJUnit3Suite() {
    final Collection<Class<?>> actual = findWithTestee(com.example.JUnitThreeSuite.class);
    final Collection<Class<?>> expected = Arrays
        .<Class<?>> asList(com.example.JUnitThreeTest.class);
    assertContains(expected, actual);
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

  @RunWith(ThrowsOnDiscoveryRunner.class)
  @SuiteClasses({ One.class, Two.class })
  static class ThrowsOnDiscoverySuite {

  }

  public static class ThrowsOnDiscoveryRunner extends Suite {

    public ThrowsOnDiscoveryRunner(final Class<?> klass, final RunnerBuilder rb)
        throws InitializationError {
      super(klass, rb);
    }

    @Override
    public Description getDescription() {
      throw new RuntimeException();
    }

  }

  private Collection<Class<?>> findWithTestee(final Class<?> clazz) {
    return this.testee.apply(clazz);
  }

  private void assertContains(final Collection<Class<?>> expected,
      final Collection<Class<?>> actual) {
    assertThat(actual).containsAll(expected);
  }

}
