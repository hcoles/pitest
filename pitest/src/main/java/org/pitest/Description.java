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
import java.util.Collection;

import org.pitest.util.Functions;
import org.pitest.util.TestInfo;

public final class Description implements Serializable {

  private static final long serialVersionUID = 1L;
  private final Class<?>    testClass;

  private final String      name;

  public Description(final String name) {
    this(name, null);
  }

  public Description(final String name, final Class<?> testClass) {
    this.testClass = testClass;
    this.name = name;
  }

  public Collection<String> getDirectTestees() {
    return TestInfo.determineTestee(this.testClass)
    .map(Functions.classToName());
  }

  public Class<?> getFirstTestClass() {
    return this.testClass;
  }

  public String getQualifiedName() {
    if (this.testClass != null) {
      return this.getFirstTestClass().getName() + "." + this.getName();
    } else {
      return this.getName();
    }
  }

  public String getName() {
    return this.name;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
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

  @Override
  public String toString() {
    return "Description [testClass=" + this.testClass + ", name=" + this.name + "]";
  }



}
