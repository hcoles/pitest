package org.pitest.coverage.codeassist;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;

/**
 * User: dima Date: Feb 8, 2009 Time: 4:26:15 PM
 */
public class ClassUtils {
  private ClassUtils() {
  }

  public static byte[] classAsBytes(final Class<?> clazz)
      throws ClassNotFoundException {
    return classAsBytes(clazz.getName());
  }

  public static byte[] classAsBytes(final String className)
      throws ClassNotFoundException {
    try {
      final URL resource = ClassUtils.class.getClassLoader().getResource(
          convertClassNameToFileName(className));
      final BufferedInputStream stream = new BufferedInputStream(
          resource.openStream());
      final byte[] result = new byte[resource.openConnection()
                                     .getContentLength()];

      int i;
      int counter = 0;
      while ((i = stream.read()) != -1) {
        result[counter] = (byte) i;
        counter++;
      }

      stream.close();

      return result;
    } catch (final IOException e) {
      throw new ClassNotFoundException("", e);
    }
  }

  private static String convertClassNameToFileName(final String className) {
    return className.replace(".", "/") + ".class";
  }

  public static Class<?> createClass(final byte[] bytes)
      throws IllegalClassFormatException {
    return new MyClassLoader().createClass(bytes);
  }

  private static final class MyClassLoader extends ClassLoader {
    public Class<?> createClass(final byte[] bytes)
        throws IllegalClassFormatException {
      return defineClass(null, bytes, 0, bytes.length);
    }
  }
}
