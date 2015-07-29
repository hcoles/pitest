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
package org.pitest.reflection;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pitest.functional.FArray;
import org.pitest.functional.predicate.Predicate;

/**
 * @author henry
 *
 */
public abstract class Reflection {

  public static Method publicMethod(final Class<?> clazz,
      final Predicate<Method> p) {
    return publicMethods(clazz, p).iterator().next();
  }

  public static Set<Method> publicMethods(final Class<?> clazz,
      final Predicate<Method> p) {
    final Set<Method> ms = new LinkedHashSet<Method>();
    FArray.filter(clazz.getMethods(), p, ms);
    return ms;
  }

  public static Set<Field> publicFields(final Class<?> clazz) {
    final Set<Field> fields = new LinkedHashSet<Field>();
    if (clazz != null) {
      fields.addAll(Arrays.asList(clazz.getFields()));
    }
    return fields;
  }

  public static Set<Method> allMethods(final Class<?> c) {
    final Set<Method> methods = new LinkedHashSet<Method>();
    if (c != null) {
      final List<Method> locallyDeclaredMethods = Arrays.asList(c
          .getDeclaredMethods());
      methods.addAll(locallyDeclaredMethods);
      methods.addAll(allMethods(c.getSuperclass()));
    }

    return methods;
  }

  public static Method publicMethod(final Class<? extends Object> clazz,
      final String name) {
    final Predicate<Method> p = new Predicate<Method>() {
      @Override
      public Boolean apply(final Method a) {
        return a.getName().equals(name);
      }

    };
    return publicMethod(clazz, p);

  }
}
