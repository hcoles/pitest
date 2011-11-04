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

import java.lang.annotation.Annotation;
import java.util.logging.Logger;

import org.pitest.functional.F;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.IsolationUtils;
import org.pitest.reflection.Reflection;

public abstract class Functions {
  private final static Logger LOG = Log.getLogger();

  public static Predicate<Class<?>> isInnerClass() {
    return new Predicate<Class<?>>() {
      public Boolean apply(final Class<?> clazz) {
        return Reflection.getParentClass(clazz).hasSome();
      }

    };
  }

  public static F<String, String> classNameToJVMClassName() {
    return new F<String, String>() {

      public String apply(final String a) {
        return a.replace(".", "/");
      }

    };
  }

  public static F<String, String> jvmClassToClassName() {
    return new F<String, String>() {

      public String apply(final String a) {
        return a.replace("/", ".");
      }

    };
  }

  public static F<Class<?>, Boolean> hasAnnotation(
      final Class<? extends Annotation> target) {
    return new F<Class<?>, Boolean>() {
      public Boolean apply(final Class<?> testClass) {
        return (testClass.getAnnotation(target) != null);
      }
    };
  }

  public static F<Class<?>, Boolean> isAssignableFrom(final Class<?> clazz) {
    return new F<Class<?>, Boolean>() {
      public Boolean apply(final Class<?> a) {
        return clazz.isAssignableFrom(a);
      }
    };
  };

  public static F<Class<?>, String> classToName() {
    return new F<Class<?>, String>() {
      public String apply(final Class<?> clazz) {
        return clazz.getName();
      }
    };

  }

  public static F<String, Option<Class<?>>> stringToClass() {
    return stringToClass(IsolationUtils.getContextClassLoader());
  }

  public static F<String, Option<Class<?>>> stringToClass(
      final ClassLoader loader) {
    return new F<String, Option<Class<?>>>() {

      public Option<Class<?>> apply(final String className) {
        try {
          final Class<?> clazz = Class.forName(className, false, loader);
          return Option.<Class<?>> some(clazz);
        } catch (final ClassNotFoundException e) {
          LOG.warning("Could not load " + className
              + " (ClassNotFoundException)");
          return Option.none();
        } catch (final NoClassDefFoundError e) {
          LOG.warning("Could not load " + className + " (NoClassDefFoundError)");
          return Option.none();
        } catch (final LinkageError e) {
          LOG.warning("Could not load " + className + " " + e.getMessage());
          return Option.none();
        } catch (final SecurityException e) {
          LOG.warning("Could not load " + className + " " + e.getMessage());
          return Option.none();
        }
      }

    };
  }

  public static Predicate<String> startsWith(final String filter) {
    return new Predicate<String>() {
      public Boolean apply(final String a) {
        return a.startsWith(filter);
      }

    };
  }

  public static Predicate<Class<?>> isInterface() {
    return new Predicate<Class<?>>() {
      public Boolean apply(final Class<?> a) {
        return a.isInterface();
      }

    };
  }

  public static <T extends Enum<T>> F<String, T> stringToEnum(
      final Class<T> clazz) {
    return new F<String, T>() {
      public T apply(final String name) {
        return Enum.valueOf(clazz, name);
      }

    };
  }

}
