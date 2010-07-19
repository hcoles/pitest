package org.pitest.internal.transformation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.pitest.util.Unchecked.translateCheckedException;

import java.lang.reflect.Method;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.pitest.extension.Transformation;
import org.pitest.extension.common.ExcludedPrefixIsolationStrategy;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.classloader.TransformingClassLoader;
import org.pitest.reflection.Reflection;

public class EnvironmentAccessTransformationTest {

  public static interface TestSystemPropertiesInterface {

    public String get(String key);

    public String get(String key, String defaultValue);

    public void set(String key, String value);

    public Properties getProperties();

    public void setProperties(Properties props);

  };

  public static class TSPIWrapper implements TestSystemPropertiesInterface {

    private final Object foreign;

    public TSPIWrapper(final Object o) {
      this.foreign = o;
    }

    public String get(final String key) {

      final Predicate<Method> p = new Predicate<Method>() {
        public Boolean apply(final Method a) {
          return a.getName().equals("get")
              && (a.getParameterTypes().length == 1);
        }
      };
      final Method m = Reflection.publicMethod(this.foreign.getClass(), p);

      try {
        return (String) m.invoke(this.foreign, key);
      } catch (final Exception e) {
        throw translateCheckedException(e);
      }

    }

    public Properties getProperties() {
      final Predicate<Method> p = new Predicate<Method>() {
        public Boolean apply(final Method a) {
          return a.getName().equals("getProperties");
        }
      };
      final Method m = Reflection.publicMethod(this.foreign.getClass(), p);

      try {
        return (Properties) m.invoke(this.foreign);
      } catch (final Exception e) {
        throw translateCheckedException(e);
      }
    }

    public void set(final String key, final String value) {
      final Predicate<Method> p = new Predicate<Method>() {
        public Boolean apply(final Method a) {
          return a.getName().equals("set");
        }
      };
      final Method m = Reflection.publicMethod(this.foreign.getClass(), p);

      try {
        m.invoke(this.foreign, key, value);
      } catch (final Exception e) {
        throw translateCheckedException(e);
      }

    }

    public void setProperties(final Properties props) {
      final Predicate<Method> p = new Predicate<Method>() {
        public Boolean apply(final Method a) {
          return a.getName().equals("setProperties");
        }
      };
      final Method m = Reflection.publicMethod(this.foreign.getClass(), p);

      try {
        m.invoke(this.foreign, props);
      } catch (final Exception e) {
        throw translateCheckedException(e);
      }

    }

    public String get(final String key, final String defaultValue) {
      final Predicate<Method> p = new Predicate<Method>() {
        public Boolean apply(final Method a) {
          return a.getName().equals("get")
              && (a.getParameterTypes().length == 2);
        }
      };
      final Method m = Reflection.publicMethod(this.foreign.getClass(), p);

      try {
        return (String) m.invoke(this.foreign, key, defaultValue);
      } catch (final Exception e) {
        throw translateCheckedException(e);
      }
    }

  };

  public static class CallsSystemGetAndSetProperty implements
      TestSystemPropertiesInterface {

    public String get(final String key) {
      return System.getProperty(key);
    }

    public void set(final String key, final String value) {
      System.setProperty(key, value);
    }

    public Properties getProperties() {
      return System.getProperties();
    }

    public void setProperties(final Properties props) {
      System.setProperties(props);
    }

    public String get(final String key, final String defaultValue) {
      return System.getProperty(key, defaultValue);
    }

  };

  private Transformation testee;

  @Before
  public void setUp() {
    this.testee = new EnvironmentAccessTransformation();
  }

  @Test
  public void testCreatesAFunctionalClass() throws Exception {
    final Object o = transformAndCreateObject(CallsSystemGetAndSetProperty.class);
    assertNotNull(o.toString());
  }

  @Test
  public void testReplacesCallsToSystemDotGetProperty() throws Exception {
    System.setProperty(getPropertyKeyForTest(), "foo");
    final TestSystemPropertiesInterface o = transformAndCreateInstance(CallsSystemGetAndSetProperty.class);
    assertEquals("foo", o.get(getPropertyKeyForTest()));
    System.setProperty(getPropertyKeyForTest(), "bar");
    assertEquals("foo", o.get(getPropertyKeyForTest()));
  }

  @Test
  public void testReplacesCallsToSystemDotGetPropertyWithDefault()
      throws Exception {
    System.setProperty(getPropertyKeyForTest(), "foo");
    final TestSystemPropertiesInterface o = transformAndCreateInstance(CallsSystemGetAndSetProperty.class);
    assertEquals("foo", o.get(getPropertyKeyForTest(), "default"));
    System.setProperty(getPropertyKeyForTest(), "bar");
    assertEquals("foo", o.get(getPropertyKeyForTest(), "default"));
    assertEquals("default", o.get("unknown_key", "default"));
  }

  @Test
  public void testReplacesCallsToSystemDotSetProperty() throws Exception {
    System.setProperty(getPropertyKeyForTest(), "foo");
    final TestSystemPropertiesInterface o = transformAndCreateInstance(CallsSystemGetAndSetProperty.class);
    o.set(getPropertyKeyForTest(), "NewValue");
    assertEquals("foo", System.getProperty(getPropertyKeyForTest()));
    assertEquals("NewValue", o.get(getPropertyKeyForTest()));
  }

  @Test
  public void testReplacesCallsToGetProperties() throws Exception {
    final TestSystemPropertiesInterface o = transformAndCreateInstance(CallsSystemGetAndSetProperty.class);
    assertNotSame(System.getProperties(), o.getProperties());
  }

  @Test
  public void testReplacesCallsTosetProperties() throws Exception {
    final TestSystemPropertiesInterface o = transformAndCreateInstance(CallsSystemGetAndSetProperty.class);
    final Properties p = new Properties();
    o.setProperties(p);
    assertSame(p, o.getProperties());
    assertNotSame(System.getProperties(), o.getProperties());
  }

  private static String getPropertyKeyForTest() {
    return EnvironmentAccessTransformationTest.class.getName();
  }

  private Object transformAndCreateObject(final Class<?> clazz)
      throws ClassNotFoundException, InstantiationException,
      IllegalAccessException {

    final TransformingClassLoader isc = new TransformingClassLoader(
        this.testee, new ExcludedPrefixIsolationStrategy(IsolatedSystem.class
            .getName()));
    final Class<?> actual = isc.loadClass(clazz.getName());
    return actual.newInstance();

  }

  private TestSystemPropertiesInterface transformAndCreateInstance(
      final Class<?> clazz) throws ClassNotFoundException,
      InstantiationException, IllegalAccessException {
    final Object o = transformAndCreateObject(clazz);
    return new TSPIWrapper(o);
  }

}
