/*
 * Copyright 2011 Henry Coles
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
package org.pitest.classinfo;

import org.pitest.util.IsolationUtils;
import org.pitest.util.Log;

import java.io.Serializable;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Stream;

public final class ClassName implements Comparable<ClassName>, Serializable {

  private static final long serialVersionUID = 1L;
  private static final Logger LOG = Log.getLogger();

  private static final ClassName OBJECT = new ClassName("java/lang/Object");
  private static final ClassName STRING = new ClassName("java/lang/String");
  private static final ClassName INTEGER = new ClassName("java/lang/Integer");
  private static final ClassName LIST = new ClassName("java/util/List");
  private static final ClassName ARRAY_LIST = new ClassName("java/util/ArrayList");
  private static final ClassName STREAM = new ClassName("java/util/stream/Stream");
  private static final ClassName FUNCTION = new ClassName("java/util/function/Function");
  private static final ClassName PREDICATE = new ClassName("java/util/function/Predicate");

  // always stored in java/lang/String "internal" format
  private final String        name;

  private ClassName(final String name) {
    this.name = name;
  }

  public static ClassName fromClass(final Class<?> clazz) {
    return ClassName.fromString(clazz.getName());
  }

  public static ClassName fromString(final String clazz) {
    final String name = clazz.replace('.', '/');
    if (name.equals(OBJECT.asInternalName())) {
      return OBJECT;
    }
    if (name.equals(STRING.asInternalName())) {
      return STRING;
    }
    if (name.equals(INTEGER.asInternalName())) {
      return INTEGER;
    }
    if (name.equals(LIST.asInternalName())) {
      return LIST;
    }
    if (name.equals(ARRAY_LIST.asInternalName())) {
      return ARRAY_LIST;
    }
    if (name.equals(STREAM.asInternalName())) {
      return STREAM;
    }
    if (name.equals(FUNCTION.asInternalName())) {
      return FUNCTION;
    }
    if (name.equals(PREDICATE.asInternalName())) {
      return PREDICATE;
    }
    return new ClassName(name);
  }


  public String asJavaName() {
    return this.name.replace('/', '.');
  }

  public String asInternalName() {
    return this.name;
  }

  public ClassName getNameWithoutPackage() {
    final int lastSeparator = this.name.lastIndexOf('/');
    if (lastSeparator != -1) {
      return ClassName.fromString(this.name.substring(lastSeparator + 1));
    }
    return this;
  }

  public ClassName getPackage() {
    final int lastSeparator = this.name.lastIndexOf('/');
    if (lastSeparator != -1) {
      return ClassName.fromString(this.name.substring(0, lastSeparator));
    }
    return ClassName.fromString("");
  }

  public ClassName withoutPrefixChars(final int prefixLength) {
    final String nameWithoutPackage = this.getNameWithoutPackage().asJavaName();
    return ClassName.fromString(this.getPackage().asJavaName()
        + "/"
        + nameWithoutPackage.substring(prefixLength));
  }

  public ClassName withoutSuffixChars(final int suffixLength) {
    final String nameWithoutPacakge = this.getNameWithoutPackage().asJavaName();
    return ClassName.fromString(this.getPackage().asJavaName()
        + "/"
        + nameWithoutPacakge.substring(0, nameWithoutPacakge.length()
            - suffixLength));
  }

  public static Function<ClassName, Stream<Class<?>>> nameToClass() {
    return nameToClass(IsolationUtils.getContextClassLoader());
  }

  public static Function<ClassName, Stream<Class<?>>> nameToClass(
      final ClassLoader loader) {
    return className -> {
      try {
        final Class<?> clazz = Class.forName(className.asJavaName(), false,
            loader);
        return Stream.of(clazz);
      } catch (final ClassNotFoundException e1) {
        LOG.warning("Could not load " + className
            + " (ClassNotFoundException: " + e1.getMessage() + ")");
        return Stream.empty();
      } catch (final NoClassDefFoundError e2) {
        LOG.warning("Could not load " + className
            + " (NoClassDefFoundError: " + e2.getMessage() + ")");
        return Stream.empty();
      } catch (final LinkageError | SecurityException e3) {
        LOG.warning("Could not load " + className + " " + e3.getMessage());
        return Stream.empty();
      }
    };
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final ClassName other = (ClassName) obj;
    return name.equals(other.name);
  }

  @Override
  public String toString() {
    return asJavaName();
  }

  @Override
  public int compareTo(final ClassName o) {
    return this.name.compareTo(o.name);
  }

}
