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

package org.pitest;

import java.io.Serializable;

import org.pitest.functional.Option;

public final class Description implements Serializable {

  private static final long        serialVersionUID = 1L;

  private final Class<?>           testClass;
  private final String             name;
  private final Option<TestMethod> method;

  public Description(final String name, final Class<?> testClass,
      final TestMethod method) {
    this.testClass = testClass;
    this.name = name;
    this.method = Option.some(method);
  }

  public Class<?> getTestClass() {
    return this.testClass;
  }

  public String getName() {
    return this.name;
  }

  public Option<TestMethod> getMethod() {
    return this.method;
  }

  @Override
  public String toString() {
    return "Description [name=" + this.name + ", testClass=" + this.testClass
        + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.method == null) ? 0 : this.method.hashCode());
    result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
    result = prime * result
        + ((this.testClass == null) ? 0 : this.testClass.hashCode());
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
    final Description other = (Description) obj;
    if (this.method == null) {
      if (other.method != null) {
        return false;
      }
    } else if (!this.method.equals(other.method)) {
      return false;
    }
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    if (this.testClass == null) {
      if (other.testClass != null) {
        return false;
      }
    } else if (!this.testClass.equals(other.testClass)) {
      return false;
    }
    return true;
  }

}
