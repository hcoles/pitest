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

package org.pitest.testapi;

import java.io.Serializable;
import java.util.Objects;

public final class Description implements Serializable {

  private static final long serialVersionUID = 1L;

  private final String testClass;
  private final String name;

  public Description(final String name) {
    this(name, (String) null);
  }

  public Description(final String name, final Class<?> testClass) {
    this(name, testClass.getName());
  }

  public Description(final String name, final String testClass) {
    this.testClass = internIfNotNull(testClass);
    this.name = name;
  }

  private String internIfNotNull(final String string) {
    if (string == null) {
      return null;
    }
    return string.intern();
  }

  public String getFirstTestClass() {
    return this.testClass;
  }

  public String getQualifiedName() {
    if ((this.testClass != null) && !this.testClass.equals(this.getName())) {
      return this.getFirstTestClass() + "." + this.getName();
    } else {
      return this.getName();
    }
  }

  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    return Objects.hash(testClass, name);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Description other = (Description) obj;
    return Objects.equals(testClass, other.testClass)
            && Objects.equals(name, other.name);
  }

  @Override
  public String toString() {
    return "Description [testClass=" + this.testClass + ", name=" + this.name
        + "]";
  }

}
