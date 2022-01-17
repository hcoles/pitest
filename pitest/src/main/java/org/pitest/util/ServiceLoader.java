package org.pitest.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

/**
 * Simple java 1.6 ServiceLoader like behaviour for 1.5 From
 * http://stackoverflow
 * .com/questions/251336/is-something-similar-to-serviceloader-in-java-1-5
 *
 */
public abstract class ServiceLoader {

  public static <S> Collection<S> load(final Class<S> ifc) {
    return load(ifc, Thread.currentThread().getContextClassLoader());
  }

  public static <S> Collection<S> load(final Class<S> ifc,
      final ClassLoader loader) {
    try {
      return loadImpl(ifc, loader);
    } catch (final IOException ex) {
      throw new PitError("Error creating service " + ifc.getName(), ex);
    }
  }

  private static <S> Collection<S> loadImpl(final Class<S> ifc,
      final ClassLoader loader) throws IOException {
    final Enumeration<URL> e = loader.getResources("META-INF/services/"
        + ifc.getName());
    final Collection<S> services = new ArrayList<>();
    while (e.hasMoreElements()) {
      final URL url = e.nextElement();
      try (InputStream is = url.openStream()) {
        createServicesFromStream(ifc, loader, services, is);
      }
    }
    return services;
  }

  private static <S> void createServicesFromStream(final Class<S> ifc,
      final ClassLoader loader, final Collection<S> services,
      final InputStream is) throws IOException {
    final BufferedReader r = new BufferedReader(new InputStreamReader(is,
            StandardCharsets.UTF_8));
    while (true) {
      String line = r.readLine();
      if (line == null) {
        break;
      }
      final int comment = line.indexOf('#');
      if (comment >= 0) {
        line = line.substring(0, comment);
      }
      final String name = line.trim();
      if (name.length() == 0) {
        continue;
      }
      if (hasFactory(name, ifc, loader)) {
        services.addAll(createServices(name, ifc, loader));
      } else {
        services.add(createService(name, ifc, loader));
      }
    }
  }

  private static <S> Collection<? extends S> createServices(String name, Class<S> ifc, ClassLoader loader) {

    try {
      final Class<?> clz = Class.forName(name, true, loader);
      Method method = clz.getMethod("factory");
      Object services = method.invoke(null);
      return (Collection<S>) services;
    } catch (ReflectiveOperationException ex) {
      throw new PitError("Error creating service " + ifc.getName(), ex);
    }
  }

  private static <S> boolean hasFactory(String name, Class<S> ifc, ClassLoader loader) {
    try {
      final Class<?> clz = Class.forName(name, true, loader);
      try {
        Method method = clz.getMethod("factory");
        return Modifier.isStatic(method.getModifiers());
      } catch (NoSuchMethodException e) {
        return false;
      }
    } catch (ClassNotFoundException ex) {
      throw new PitError("Error creating service " + ifc.getName(), ex);
    }
  }

  private static <S> S createService(final String name, final Class<S> ifc,
      final ClassLoader loader) {
    try {
      final Class<?> clz = Class.forName(name, true, loader);
      if (Enum.class.isAssignableFrom(clz)) {
        return firstEnumInstance((Class<? extends Enum>) clz);
      }
      final Class<? extends S> impl = clz.asSubclass(ifc);
      final Constructor<? extends S> ctor = impl.getConstructor();
      return ctor.newInstance();
    } catch (final Exception ex) {
      throw new PitError("Error creating service " + ifc.getName(), ex);
    }
  }

  private static <S> S firstEnumInstance(Class<? extends Enum> clz) {
      return (S) clz.getEnumConstants()[0];
  }

}
