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

import java.util.logging.Logger;

import org.pitest.functional.F;
import org.pitest.functional.Option;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Log;

public final class ClassName implements Comparable<ClassName> {

  private static final Logger LOG = Log.getLogger();
  
  private static final ClassName OBJECT = new ClassName("java/lang/Object");
  private static final ClassName STRING = new ClassName("java/lang/String");

  private final String        name;

  private ClassName(final String name) {
    this.name = name;
  }

  public static ClassName fromClass(final Class<?> clazz) {
    return ClassName.fromString(clazz.getName());
  }
  
  public static ClassName fromString(final String clazz) {
    String name = clazz.replace('.', '/');
    if (name.equals(OBJECT.asInternalName())) {
      return OBJECT;
    }
    if (name.equals(STRING.asInternalName())) {
      return STRING;
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
      return ClassName.fromString(this.name.substring(lastSeparator + 1,
          this.name.length()));
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
        + nameWithoutPackage.substring(prefixLength,
            nameWithoutPackage.length()));
  }

  public ClassName withoutSuffixChars(final int suffixLength) {
    final String nameWithoutPacakge = this.getNameWithoutPackage().asJavaName();
    return ClassName.fromString(this.getPackage().asJavaName()
        + "/"
        + nameWithoutPacakge.substring(0, nameWithoutPacakge.length()
            - suffixLength));
  }

  public static F<String, ClassName> stringToClassName() {
    return new F<String, ClassName>() {
      @Override
      public ClassName apply(final String clazz) {
        return ClassName.fromString(clazz);
      }
    };
  }

  public static F<ClassName, Option<Class<?>>> nameToClass() {
    return nameToClass(IsolationUtils.getContextClassLoader());
  }

  public static F<ClassName, Option<Class<?>>> nameToClass(
      final ClassLoader loader) {
    return new F<ClassName, Option<Class<?>>>() {

      @Override
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

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.name == null) ? 0 : this.name.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final ClassName other = (ClassName) obj;
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return asJavaName();
  }

  @Override
  public int compareTo(final ClassName o) {
    return this.asJavaName().compareTo(o.asJavaName());
  }

}
