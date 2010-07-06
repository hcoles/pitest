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

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.reflection.Reflection;

public class IsolationUtils {

  public static boolean fromDifferentLoader(final Class<?> clazz,
      final ClassLoader loader) {
    try {
      return clazz != loader.loadClass(clazz.getName());
    } catch (final ClassNotFoundException ex) {
      throw new RuntimeException(ex);
    }
  }

  private static Class<?> convertForClassLoader(final ClassLoader loader,
      final String name) {
    try {
      return Class.forName(name, true, loader);
    } catch (final ClassNotFoundException ex) {
      throw new RuntimeException(ex);
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
            && a.getReturnType().getName().equals(m.getReturnType().getName())) {
          final List<String> parameters = FCollection.map(Arrays.asList(a
              .getParameterTypes()), f);
          return parameters.equals(params);
        }
        return false;

      }

    };
    final List<Method> matches = FCollection.filter(Reflection.allMethods(c2),
        p);
    // FIXME check length exactly 1
    return matches.get(0);

  }

}
