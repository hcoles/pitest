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

import java.util.logging.Logger;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;

public abstract class Functions {
  private final static Logger LOG = Log.getLogger();

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

  public static F<Class<?>, String> classToName() {
    return new F<Class<?>, String>() {
      public String apply(final Class<?> clazz) {
        return clazz.getName();
      }
    };

  }

  public static F<ClassName, Option<Class<?>>> nameToClass() {
    return nameToClass(IsolationUtils.getContextClassLoader());
  }

  public static F<ClassName, Option<Class<?>>> nameToClass(
      final ClassLoader loader) {
    return new F<ClassName, Option<Class<?>>>() {

      public Option<Class<?>> apply(final ClassName className) {
        try {
          final Class<?> clazz = Class.forName(className.asJavaName(), false,
              loader);
          return Option.<Class<?>> some(clazz);
        } catch (final ClassNotFoundException e) {
          LOG.warning("Could not load " + className
              + " (ClassNotFoundException: " + e.getMessage() + ")");
          return Option.none();
        } catch (final NoClassDefFoundError e) {
          LOG.warning("Could not load " + className
        		  + " (NoClassDefFoundError: " + e.getMessage() + ")");
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

  public static <T extends Enum<T>> F<String, T> stringToEnum(
      final Class<T> clazz) {
    return new F<String, T>() {
      public T apply(final String name) {
        return Enum.valueOf(clazz, name);
      }

    };
  }

}
