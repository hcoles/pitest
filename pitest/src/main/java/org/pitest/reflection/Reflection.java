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

import static org.pitest.util.Functions.jvmClassToClassName;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pitest.functional.FArray;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.ClassByteArraySource;

/**
 * @author henry
 * 
 */
public abstract class Reflection {

  public static Class<?> getTopClass(final Class<?> clazz) {
    final Option<Class<?>> parent = getParentClass(clazz);
    if (parent.hasNone()) {
      return clazz;
    } else {
      return getTopClass(parent.value());
    }
  }

  public static Option<Class<?>> getParentClass(final Class<?> clazz) {
    Class<?> outerClass = clazz.getEnclosingClass();
    if (outerClass == null) {
      final Method outerMethod = clazz.getEnclosingMethod();
      if (outerMethod != null) {
        outerClass = outerMethod.getDeclaringClass();
      }

    }
    return Option.<Class<?>> some(outerClass);
  }

  public static Collection<String> allInnerClasses(final Class<?> clazz,
      final ClassByteArraySource source) {
    if (source.apply(clazz.getName()).hasSome()) {
      return FCollection.map(InnerClassVisitor.getInnerClasses(source.apply(
          clazz.getName()).value()), jvmClassToClassName());
    } else {
      System.err.println("Could not find bytes for " + clazz);
      return Collections.emptyList();
    }

  }

  public static Method publicMethod(final Class<?> clazz,
      final Predicate<Method> p) {
    return publicMethods(clazz, p).iterator().next();
  }

  public static Set<Field> publicFields(final Class<?> clazz,
      final Predicate<Field> p) {
    final Set<Field> fs = new LinkedHashSet<Field>();
    FArray.filter(clazz.getFields(), p, fs);
    return fs;
  }

  public static Set<Method> publicMethods(final Class<?> clazz,
      final Predicate<Method> p) {
    final Set<Method> ms = new LinkedHashSet<Method>();
    FArray.filter(clazz.getMethods(), p, ms);
    return ms;
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
      public Boolean apply(final Method a) {
        return a.getName().equals(name);
      }

    };
    return publicMethod(clazz, p);

  }

  public static Predicate<Class<?>> isTopClass() {
    return new Predicate<Class<?>>() {
      public Boolean apply(Class<?> a) {
        return isTopClass(a);
      }

    };
  }

  public static boolean isTopClass(final Class<?> clazz) {
    return getTopClass(clazz).equals(clazz);
  }

}
