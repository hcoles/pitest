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

import java.io.Serializable;

import org.pitest.functional.F;

public final class ClassName implements Serializable, Comparable<ClassName> {

  private static final long serialVersionUID = 1L;

  private final String      name;

  public ClassName(final String name) {
    this.name = name.replace('.', '/').intern();
  }

  public ClassName(final Class<?> clazz) {
    this(clazz.getName());
  }

  public static ClassName fromString(final String clazz) {
    return new ClassName(clazz);
  }

  public static ClassName fromClass(final Class<?> clazz) {
    return new ClassName(clazz);
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
      return new ClassName(this.name.substring(lastSeparator + 1,
          this.name.length()));
    }
    return this;
  }

  public ClassName getPackage() {
    final int lastSeparator = this.name.lastIndexOf('/');
    if (lastSeparator != -1) {
      return new ClassName(this.name.substring(0, lastSeparator));
    }
    return new ClassName("");
  }

  public ClassName withoutPrefixChars(final int prefixLength) {
    final String nameWithoutPackage = this.getNameWithoutPackage().asJavaName();
    return new ClassName(this.getPackage().asJavaName()
        + "/"
        + nameWithoutPackage.substring(prefixLength,
            nameWithoutPackage.length()));
  }

  public ClassName withoutSuffixChars(final int suffixLength) {
    final String nameWithoutPacakge = this.getNameWithoutPackage().asJavaName();
    return new ClassName(this.getPackage().asJavaName()
        + "/"
        + nameWithoutPacakge.substring(0, nameWithoutPacakge.length()
            - suffixLength));
  }

  public static F<String, ClassName> stringToClassName() {
    return new F<String, ClassName>() {

      public ClassName apply(final String clazz) {
        return ClassName.fromString(clazz);
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

  public int compareTo(final ClassName o) {
    return this.asJavaName().compareTo(o.asJavaName());
  }

}
