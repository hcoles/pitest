/*
 * Copyright 2010 Henry Coles
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, 
 * software distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and limitations under the License. 
 */

package org.pitest.internal;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.WeakHashMap;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.reflection.Reflection;
import org.pitest.util.Base64;
import org.pitest.util.Unchecked;

import com.thoughtworks.xstream.XStream;

public class IsolationUtils {

  private final static XStream                           xstreamInstance = new XStream();
  private final static WeakHashMap<ClassLoader, XStream> cache           = new WeakHashMap<ClassLoader, XStream>();

  public static ClassLoader getContextClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  public static void setContextClassLoader(final ClassLoader loader) {
    Thread.currentThread().setContextClassLoader(loader);
  }

  public static Object cloneForLoader(final Object o, final ClassLoader loader) {
    try {
      final String xml = toXml(o);
      final XStream foreginXstream = getXStreamForLoader(loader);
      return foreginXstream.fromXML(xml);
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private static XStream getXStreamForLoader(final ClassLoader loader) {
    XStream foreginXstream = cache.get(loader);
    if (foreginXstream == null) {
      foreginXstream = new XStream();
      foreginXstream.setClassLoader(loader);
      // possible that more than one instance will be created
      // per loader, but probably better than synchronizing the whole method
      synchronized (cache) {
        cache.put(loader, foreginXstream);
      }
    }
    return foreginXstream;
  }

  public static boolean fromDifferentLoader(final Class<?> clazz,
      final ClassLoader loader) {
    try {
      return clazz != loader.loadClass(clazz.getName());
    } catch (final ClassNotFoundException ex) {
      throw translateCheckedException(ex);
    }
  }

  private static Class<?> convertForClassLoader(final ClassLoader loader,
      final String name) {
    try {
      return Class.forName(name, true, loader);
    } catch (final ClassNotFoundException ex) {
      throw translateCheckedException(ex);
    }
  }

  public static Class<?> convertForClassLoader(final ClassLoader loader,
      final Class<?> clazz) {
    if (clazz.getClassLoader() != loader) {
      return convertForClassLoader(loader, clazz.getName());
    } else {
      return clazz;
    }

  }

  public static Method convertForClassLoader(final ClassLoader loader,
      final Method m) {

    if (loader != m.getDeclaringClass().getClassLoader()) {
      final Class<?> c2 = convertForClassLoader(loader, m.getDeclaringClass());

      final F<Class<?>, String> f = new F<Class<?>, String>() {
        public String apply(final Class<?> a) {
          return a.getName();
        }
      };

      final List<String> params = FCollection.map(Arrays.asList(m
          .getParameterTypes()), f);

      final F<Method, Boolean> p = new F<Method, Boolean>() {
        public Boolean apply(final Method a) {
          if (a.getName().equals(m.getName())
              && a.getReturnType().getName()
                  .equals(m.getReturnType().getName())) {
            final List<String> parameters = FCollection.map(Arrays.asList(a
                .getParameterTypes()), f);
            return parameters.equals(params);
          }
          return false;

        }

      };
      final List<Method> matches = FCollection.filter(
          Reflection.allMethods(c2), p);
      // FIXME check length exactly 1
      return matches.get(0);
    } else {
      return m;
    }

  }

  public static String toXml(final Object o) {
    return xstreamInstance.toXML(o);
  }

  public static String toTransportString(final Object o) {
    try {
      return Base64.encodeBytes(toXml(o).getBytes("UTF-8"));
    } catch (final UnsupportedEncodingException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public static String decodeTransportString(final String encodedXml)
      throws IOException {
    return new String(Base64.decode(encodedXml), "UTF-8");
  }

  public static Object fromTransportString(final String encodedXml) {
    try {
      return fromXml(decodeTransportString(encodedXml));
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public static Object fromXml(final String xml) {
    return xstreamInstance.fromXML(xml);
  }

  public static Object fromXml(final String xml, final ClassLoader loader) {
    final XStream xstream = getXStreamForLoader(loader);
    return xstream.fromXML(xml);
  }

}
