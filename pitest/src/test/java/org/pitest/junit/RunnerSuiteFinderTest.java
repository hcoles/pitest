package org.pitest.junit;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Arrays;
import java.util.Collection;

import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.pitest.internal.TestClass;

import com.example.TheoryTest;

public class RunnerSuiteFinderTest {

  private RunnerSuiteFinder testee;

  @Before
  public void setup() {
    this.testee = new RunnerSuiteFinder();
  }

  @Test
  public void shouldNotFindClassesInJUnitTheoryTest() {
    final Collection<TestClass> actual = findWithTestee(TheoryTest.class);
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
    final Collection<TestClass> actual = findWithTestee(CustomSuite.class);
    final Collection<TestClass> expected = Arrays.asList(new TestClass(
        One.class), new TestClass(Two.class));

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
    final Collection<TestClass> actual = findWithTestee(JUnit3Suite.class);
    final Collection<TestClass> expected = Arrays.asList(new TestClass(
        One.class), new TestClass(Two.class));
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
    final Collection<TestClass> actual = findWithTestee(JUnit3SuiteMethod.class);
    final Collection<TestClass> expected = Arrays.asList(new TestClass(
        One.class), new TestClass(Two.class));
    assertContains(expected, actual);
  }

  @Test
  public void shouldFindSuiteClasseInNestedJUnit3Suite() {
    final Collection<TestClass> actual = findWithTestee(com.example.JUnitThreeSuite.class);
    final Collection<TestClass> expected = Arrays.asList(new TestClass(
        com.example.JUnitThreeTest.class));
    assertContains(expected, actual);
  }

  private Collection<TestClass> findWithTestee(final Class<?> clazz) {
    return this.testee.apply(new TestClass(clazz));
  }

  private void assertContains(final Collection<TestClass> expected,
      final Collection<TestClass> actual) {
    assertThat(actual,
        hasItems(expected.toArray(new TestClass[expected.size()])));
  }

}
