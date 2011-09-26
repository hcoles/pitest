package org.pitest.internal.transformation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;

import java.util.Properties;
import java.util.concurrent.Callable;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.pitest.extension.Transformation;
import org.pitest.extension.common.ExcludedPrefixIsolationStrategy;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.TransformingClassLoader;
import org.pitest.internal.isolation.IsolatedLong;
import org.pitest.internal.isolation.IsolatedSystem;

public class EnvironmentAccessTransformationTest {

  private Transformation testee;

  @Before
  public void setUp() {
    this.testee = new EnvironmentAccessTransformation();
  }

  @Test
  public void shouldReplaceCallsToSystemDotGetProperty() throws Exception {
    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return System.getProperty(getPropertyKeyForTest());
      }
    };

    assertEnvironmentIsIsolated(c, "foo", "bar");

  }

  @Test
  public void shouldReplaceCallsToSystemDotGetPropertyWithDefault()
      throws Exception {
    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return System.getProperty(getPropertyKeyForTest(), "default");
      }
    };

    assertEnvironmentIsIsolated(c, "foo", "bar");
  }

  @Test
  public void shouldReplaceCallsToSystemDotSetProperty() throws Exception {
    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        System.setProperty(getPropertyKeyForTest(), "foo");
        return null;
      }
    };
    final ClassLoader loader = createClassLoaderAndEnsureIsolatedSystemIsLoaded();
    final String expected = System.getProperty(getPropertyKeyForTest());
    runInClassLoader(loader, c);
    assertSame(expected, System.getProperty(getPropertyKeyForTest()));
  }

  @Test
  public void shouldReplaceCallsToGetProperties() throws Exception {
    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return System.getProperties();
      }
    };
    final ClassLoader loader = createClassLoaderAndEnsureIsolatedSystemIsLoaded();
    final Properties actual = (Properties) runInClassLoader(loader, c);
    assertNotSame(c.call(), actual);
  }

  @Test
  public void shouldReplaceCallsToSetProperties() throws Exception {
    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        System.setProperties(new Properties());
        return null;
      }
    };
    final ClassLoader loader = createClassLoaderAndEnsureIsolatedSystemIsLoaded();
    final Properties expected = System.getProperties();
    runInClassLoader(loader, c);
    assertSame(expected, System.getProperties());
  }

  @Test
  public void shouldReplaceCallsToGetBoolean() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Boolean.getBoolean(getPropertyKeyForTest());
      }
    };

    assertEnvironmentIsIsolated(c, true, false);

  }

  @Test
  public void testReplaceCallsToGetLong() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Long.getLong(getPropertyKeyForTest());
      }

    };
    assertEnvironmentIsIsolated(c, 1l, 2l);

  }

  @Test
  public void shouldReplaceCallsToGetLongObjectLong() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Long.getLong(getPropertyKeyForTest(),
            Long.valueOf(Long.MAX_VALUE));
      }

    };
    assertEnvironmentIsIsolated(c, 1l, 2l);

  }

  @Test
  @Ignore
  // not clear why this is failing, looks like a bug
  // in ASM, but this seems unlikely. Revisit with additional coffee
  public void shouldReplaceCallToGetLongPrimitiveLong() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Long.getLong(getPropertyKeyForTest(), Long.MAX_VALUE);
      }

    };
    assertEnvironmentIsIsolated(c, 1l, 2l);

  }

  @Test
  public void shouldReplaceCallToGetInteger() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Integer.getInteger(getPropertyKeyForTest());
      }

    };
    assertEnvironmentIsIsolated(c, 1, 2);

  }

  @Test
  public void shouldReplaceCallsToGetIntegerObjectInteger() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Integer.getInteger(getPropertyKeyForTest(), Integer.valueOf(1));
      }

    };
    assertEnvironmentIsIsolated(c, 1, 2);

  }

  @Test
  public void shouldReplaceCallsToGetIntegerPrimitiveInteger() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Integer.getInteger(getPropertyKeyForTest(), Integer.MAX_VALUE);
      }

    };
    assertEnvironmentIsIsolated(c, 1, 2);

  }

  private void assertEnvironmentIsIsolated(final Callable<Object> c,
      final Object valueOne, final Object valueTwo)
      throws ClassNotFoundException, Exception {
    System.setProperty(getPropertyKeyForTest(), valueOne.toString());
    final ClassLoader loader = createClassLoaderAndEnsureIsolatedSystemIsLoaded();
    assertEquals(valueOne, runInClassLoader(getNormalLoader(), c));
    assertEquals(valueOne, runInClassLoader(loader, c));
    System.setProperty(getPropertyKeyForTest(), valueTwo.toString());
    assertEquals(valueTwo, runInClassLoader(getNormalLoader(), c));
    assertEquals(valueOne, runInClassLoader(loader, c));
  }

  private ClassLoader getNormalLoader() {
    return IsolationUtils.getContextClassLoader();
  }

  private ClassLoader createClassLoaderAndEnsureIsolatedSystemIsLoaded()
      throws ClassNotFoundException {
    final TransformingClassLoader loader = new TransformingClassLoader(
        this.testee, new ExcludedPrefixIsolationStrategy(
            IsolatedSystem.class.getName(), IsolatedLong.class.getName()));
    Class.forName(IsolatedSystem.class.getName(), true, loader);
    return loader;
  }

  @SuppressWarnings("unchecked")
  private Object runInClassLoader(final ClassLoader loader,
      final Callable<Object> callable) throws Exception {
    final Callable<Object> c = (Callable<Object>) IsolationUtils
        .cloneForLoader(callable, loader);
    return c.call();

  }

  private static String getPropertyKeyForTest() {
    return EnvironmentAccessTransformationTest.class.getName();
  }

}
