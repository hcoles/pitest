package org.pitest.extension.common;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.annotations.PITSuite;
import org.pitest.extension.common.testsuitefinder.PITStaticMethodSuiteFinder;
import org.pitest.internal.TestClass;

public class PITStaticMethodSuiteFinderTest {

  public static class HasAnnotatedMethod {
    @PITSuite
    public static List<Class<?>> suite() {
      return Arrays.<Class<?>> asList(String.class, Integer.class);
    }
  }

  @Test
  public void shouldReturnAllClassesReturnedByAnnotatedMethod() {
    final PITStaticMethodSuiteFinder testee = new PITStaticMethodSuiteFinder();
    final TestClass root = new TestClass(HasAnnotatedMethod.class);
    final Collection<TestClass> actual = testee.apply(root);
    final Collection<TestClass> expected = Arrays.asList(new TestClass(
        String.class), new TestClass(Integer.class));
    assertEquals(expected, actual);
  }

  public static class NoAnnotatedMethod {

    public static List<Class<?>> suite() {
      return Arrays.<Class<?>> asList(String.class, Integer.class);
    }
  }

  @Test
  public void shouldReturnEmptyListIfNoAnnotatedMethodPresent() {
    final PITStaticMethodSuiteFinder testee = new PITStaticMethodSuiteFinder();
    final TestClass root = new TestClass(NoAnnotatedMethod.class);
    assertEquals(Collections.emptyList(), testee.apply(root));
  }

}
