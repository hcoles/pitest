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

import java.util.function.Function;
import java.util.function.Predicate;

public abstract class Functions {

  public static Function<String, String> classNameToJVMClassName() {
    return new Function<String, String>() {

      @Override
      public String apply(final String a) {
        return a.replace(".", "/");
      }

    };
  }

  public static Function<String, String> jvmClassToClassName() {
    return new Function<String, String>() {

      @Override
      public String apply(final String a) {
        return a.replace("/", ".");
      }

    };
  }

  public static Function<Class<?>, String> classToName() {
    return new Function<Class<?>, String>() {
      @Override
      public String apply(final Class<?> clazz) {
        return clazz.getName();
      }
    };

  }

  public static Predicate<String> startsWith(final String filter) {
    return new Predicate<String>() {
      @Override
      public boolean test(final String a) {
        return a.startsWith(filter);
      }

    };
  }

  public static <T extends Enum<T>> Function<String, T> stringToEnum(
      final Class<T> clazz) {
    return new Function<String, T>() {
      @Override
      public T apply(final String name) {
        return Enum.valueOf(clazz, name);
      }

    };
  }
}
