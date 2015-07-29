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

package org.pitest.util;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.StringWriter;
import java.io.Writer;
import java.util.WeakHashMap;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.CompactWriter;

public abstract class IsolationUtils {

  private static final XStream                           XSTREAM_INSTANCE          = new XStream(
      new PitXmlDriver());
  private static final WeakHashMap<ClassLoader, XStream> CACHE                     = new WeakHashMap<ClassLoader, XStream>();
  private static final ClassLoaderDetectionStrategy      LOADER_DETECTION_STRATEGY = new ClassLoaderDetectionStrategy() {

    @Override
                                                                                     public boolean fromDifferentLoader(
        final Class<?> clazz,
        final ClassLoader loader) {
      return IsolationUtils
          .fromIncompatibleLoader(
              clazz,
              loader);
    }

  };

  public static ClassLoaderDetectionStrategy loaderDetectionStrategy() {
    return LOADER_DETECTION_STRATEGY;
  }

  public static ClassLoader getContextClassLoader() {
    return Thread.currentThread().getContextClassLoader();
  }

  public static Object cloneForLoader(final Object o, final ClassLoader loader) {
    try {
      final String xml = toXml(o);
      final XStream foreignXstream = getXStreamForLoader(loader);
      return foreignXstream.fromXML(xml);
    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }

  }

  private static XStream getXStreamForLoader(final ClassLoader loader) {
    XStream foreginXstream = CACHE.get(loader);
    if (foreginXstream == null) {
      foreginXstream = new XStream(new PitXmlDriver());
      foreginXstream.setClassLoader(loader);
      // possible that more than one instance will be created
      // per loader, but probably better than synchronizing the whole method
      synchronized (CACHE) {
        CACHE.put(loader, foreginXstream);
      }
    }
    return foreginXstream;
  }

  private static boolean fromIncompatibleLoader(final Class<?> clazz,
      final ClassLoader loader) {

    ClassLoader target = clazz.getClassLoader();
    if ((target == IsolationUtils.bootClassLoader())
        || (loader == IsolationUtils.bootClassLoader())) {
      return false;
    }

    while (target != IsolationUtils.bootClassLoader()) {
      if (target == loader) {
        return false;
      }
      target = target.getParent();
    }
    return true;
  }

  public static Class<?> convertForClassLoader(final ClassLoader loader,
      final String name) {
    try {
      return Class.forName(name, false, loader);
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

  public static String toXml(final Object o) {
    final Writer writer = new StringWriter();
    XSTREAM_INSTANCE.marshal(o, new CompactWriter(writer));

    return writer.toString();
  }

  public static Object fromXml(final String xml) {
    return XSTREAM_INSTANCE.fromXML(xml);
  }

  public static Object clone(final Object object) {
    return cloneForLoader(object, getContextClassLoader());
  }

  public static ClassLoader bootClassLoader() {
    return Object.class.getClassLoader();
  }

}
