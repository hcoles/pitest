package org.pitest.util;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Contains methods that help with hacking at the class loaders.
 *
 * @author Aidan Morgan
 */
public class ClasspathUtil {
  private ClasspathUtil() {

  }

  /**
   * Adds the resource identified by the provided {@see String} to the {@see ClassLoder.getSystemClassLoader} instance.
   *
   * Adds the resource by assuming that the system {@see ClassLoader} is a {@see URLClassLoader}
   * and using reflection to invoke the {@see URLClassLoader.addURL} method.
   *
   * @param s the resource to add to the classpath.
   */
  public static void addPath(String s) {
    File f = new File(s);

    try {
      URL u = f.toURI().toURL();

      URLClassLoader urlClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();

      Class urlClass = URLClassLoader.class;
      Method method = urlClass.getDeclaredMethod("addURL", new Class[]{URL.class});

      method.setAccessible(true);
      method.invoke(urlClassLoader, new Object[]{u});
    } catch (MalformedURLException e) {
      throw new IllegalArgumentException("Cannot add File " + f.getAbsolutePath() + " to classpath.", e);
    } catch (NoSuchMethodException e) {
      throw new IllegalStateException("Cannot call addURL on URLClassLoader as the SystemClassLoader may not be an instance of URLClassLoader.", e);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException("Cannot call addURL on URLClassLoader as the SystemClassLoader may not be an instance of URLClassLoader.", e);
    } catch (InvocationTargetException e) {
      throw new IllegalStateException("Cannot call addURL on URLClassLoader as the SystemClassLoader may not be an instance of URLClassLoader.", e);
    }
  }

}
