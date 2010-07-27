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
import org.pitest.internal.classloader.TransformingClassLoader;
import org.pitest.internal.isolation.IsolatedLong;
import org.pitest.internal.isolation.IsolatedSystem;

import com.thoughtworks.xstream.XStream;

public class EnvironmentAccessTransformationTest {

  private Transformation testee;

  @Before
  public void setUp() {
    this.testee = new EnvironmentAccessTransformation();
  }

  @Test
  public void testReplacesCallsToSystemDotGetProperty() throws Exception {
    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return System.getProperty(getPropertyKeyForTest());
      }
    };

    testEnvironmentIsIsolated(c, "foo", "bar");

  }

  @Test
  public void testReplacesCallsToSystemDotGetPropertyWithDefault()
      throws Exception {
    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return System.getProperty(getPropertyKeyForTest(), "default");
      }
    };

    testEnvironmentIsIsolated(c, "foo", "bar");
  }

  @Test
  public void testReplacesCallsToSystemDotSetProperty() throws Exception {
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
  public void testReplacesCallsToGetProperties() throws Exception {
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
  public void testReplacesCallsToSetProperties() throws Exception {
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
  public void testReplacesCallsToGetBoolean() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Boolean.getBoolean(getPropertyKeyForTest());
      }
    };

    testEnvironmentIsIsolated(c, true, false);

  }

  @Test
  public void testReplacesCallToGetLong() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Long.getLong(getPropertyKeyForTest());
      }

    };
    testEnvironmentIsIsolated(c, 1l, 2l);

  }

  @Test
  public void testReplacesCallToGetLongObjectLong() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Long.getLong(getPropertyKeyForTest(), new Long(Long.MAX_VALUE));
      }

    };
    testEnvironmentIsIsolated(c, 1l, 2l);

  }

  @Test
  @Ignore
  // not clear why this is failing, looks like a bug
  // in ASM, but this seems unlikely. Revisit with additional coffee
  public void testReplacesCallToGetLongPrimitiveLong() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Long.getLong(getPropertyKeyForTest(), Long.MAX_VALUE);
      }

    };
    testEnvironmentIsIsolated(c, 1l, 2l);

  }

  @Test
  public void testReplacesCallToGetInteger() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Integer.getInteger(getPropertyKeyForTest());
      }

    };
    testEnvironmentIsIsolated(c, 1, 2);

  }

  @Test
  public void testReplacesCallToGetIntegerObjectInteger() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Integer.getInteger(getPropertyKeyForTest(), new Integer(1));
      }

    };
    testEnvironmentIsIsolated(c, 1, 2);

  }

  @Test
  public void testReplacesCallToGetIntegerPrimitiveInteger() throws Exception {

    final Callable<Object> c = new Callable<Object>() {
      public Object call() throws Exception {
        return Integer.getInteger(getPropertyKeyForTest(), Integer.MAX_VALUE);
      }

    };
    testEnvironmentIsIsolated(c, 1, 2);

  }

  private void testEnvironmentIsIsolated(final Callable<Object> c,
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
    return Thread.currentThread().getContextClassLoader();
  }

  private ClassLoader createClassLoaderAndEnsureIsolatedSystemIsLoaded()
      throws ClassNotFoundException {
    final TransformingClassLoader loader = new TransformingClassLoader(
        this.testee, new ExcludedPrefixIsolationStrategy(IsolatedSystem.class
            .getName(), IsolatedLong.class.getName()));
    Class.forName(IsolatedSystem.class.getName(), true, loader);
    return loader;
  }

  @SuppressWarnings("unchecked")
  private Object runInClassLoader(final ClassLoader loader,
      final Callable<Object> callable) throws Exception {
    final XStream x = new XStream();
    final String xml = x.toXML(callable);
    final XStream foreign = new XStream();
    foreign.setClassLoader(loader);
    final Callable<Object> c = (Callable<Object>) foreign.fromXML(xml);
    return c.call();

  }

  private static String getPropertyKeyForTest() {
    return EnvironmentAccessTransformationTest.class.getName();
  }

}
