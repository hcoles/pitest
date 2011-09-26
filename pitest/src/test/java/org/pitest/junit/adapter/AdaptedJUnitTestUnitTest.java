package org.pitest.junit.adapter;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.extension.ResultCollector;
import org.pitest.functional.Option;
import org.pitest.internal.ClassLoaderDetectionStrategy;
import org.pitest.internal.IsolationUtils;

public class AdaptedJUnitTestUnitTest {

  private AdaptedJUnitTestUnit testee;

  @Mock
  ResultCollector              rc;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  private static class HideFromJUnit {

    public static class JUnit4Test {

      @SuppressWarnings("unused")
      @Test
      public void pass() {

      }

      @SuppressWarnings("unused")
      @Test
      public void fail() {
        org.junit.Assert.fail();
      }

    }

  }

  @Test
  public void shouldCallNotifyStartWhenExecutingInOwnClassLoader() {
    verifyNotifyStart(IsolationUtils.loaderDetectionStrategy());
  }

  @Test
  public void shouldCallNotifyStartWhenExecutingInForeignClassLoader() {
    verifyNotifyStart(neverMatchLoaderDetectionStrategy());
  }

  private void verifyNotifyStart(
      final ClassLoaderDetectionStrategy classLoaderDetectionStrategy) {
    createTestee(classLoaderDetectionStrategy, HideFromJUnit.JUnit4Test.class,
        "pass");
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyStart(this.testee.getDescription());
  }

  @Test
  public void shouldCallNotifyEndOnSuccessWhenExecutingInOwnClassLoader() {
    verifyNotifyEnd(IsolationUtils.loaderDetectionStrategy());
  }

  @Test
  public void shouldCallNotifyEndOnSuccessWhenExecutingInForeignClassLoader() {
    verifyNotifyEnd(neverMatchLoaderDetectionStrategy());
  }

  private void verifyNotifyEnd(
      final ClassLoaderDetectionStrategy classLoaderDetectionStrategy) {
    createTestee(classLoaderDetectionStrategy, HideFromJUnit.JUnit4Test.class,
        "pass");
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyEnd(this.testee.getDescription());
  }

  @Test
  public void shouldCallNotifyEndWithThrowableOnFailureWhenExecutingInOwnClassLoader() {
    verifyNotifyEndWithThrowable(IsolationUtils.loaderDetectionStrategy());
  }

  @Test
  public void shouldCallNotifyEndWithThrowableOnFailureWhenExecutingInForeignClassLoader() {
    verifyNotifyEndWithThrowable(neverMatchLoaderDetectionStrategy());
  }

  private void verifyNotifyEndWithThrowable(
      final ClassLoaderDetectionStrategy classLoaderDetectionStrategy) {
    createTestee(classLoaderDetectionStrategy, HideFromJUnit.JUnit4Test.class,
        "fail");
    this.testee.execute(IsolationUtils.getContextClassLoader(), this.rc);
    verify(this.rc).notifyEnd(eq(this.testee.getDescription()),
        any(Throwable.class));
  }

  private void createTestee(
      final ClassLoaderDetectionStrategy classLoaderDetectionStrategy,
      final Class<?> clazz, final String method) {
    this.testee = new AdaptedJUnitTestUnit(classLoaderDetectionStrategy, clazz,
        createFilter(clazz, method));
  }

  private Option<Filter> createFilter(final Class<?> clazz, final String method) {
    final Description d = Description.createTestDescription(clazz, method);
    final Filter f = new Filter() {

      @Override
      public boolean shouldRun(final Description description) {
        return d.toString().equals(description.toString());
      }

      @Override
      public String describe() {
        return null;
      }

    };
    return Option.some(f);
  }

  private ClassLoaderDetectionStrategy neverMatchLoaderDetectionStrategy() {
    return new ClassLoaderDetectionStrategy() {

      public boolean fromDifferentLoader(final Class<?> clazz,
          final ClassLoader loader) {
        return true;
      }

    };
  }

}
