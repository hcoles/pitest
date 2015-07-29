/*
 * Copyright 2013 Henry Coles
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
package org.pitest.mutationtest.engine;

import org.pitest.classinfo.ClassName;

/**
 * The co-ordinates of a method within a class.
 *
 */
public final class Location implements Comparable<Location> {

  private final ClassName  clazz;
  private final MethodName method;
  private final String     methodDesc;

  public Location(final ClassName clazz, final MethodName method,
      final String methodDesc) {
    this.clazz = clazz;
    this.method = method;
    this.methodDesc = methodDesc;
  }

  public static Location location(final ClassName clazz,
      final MethodName method, final String methodDesc) {
    return new Location(clazz, method, methodDesc);
  }

  public ClassName getClassName() {
    return this.clazz;
  }

  public MethodName getMethodName() {
    return this.method;
  }

  public String getMethodDesc() {
    return this.methodDesc;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.clazz == null) ? 0 : this.clazz.hashCode());
    result = (prime * result)
        + ((this.method == null) ? 0 : this.method.hashCode());
    result = (prime * result)
        + ((this.methodDesc == null) ? 0 : this.methodDesc.hashCode());
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
    final Location other = (Location) obj;
    if (this.clazz == null) {
      if (other.clazz != null) {
        return false;
      }
    } else if (!this.clazz.equals(other.clazz)) {
      return false;
    }
    if (this.method == null) {
      if (other.method != null) {
        return false;
      }
    } else if (!this.method.equals(other.method)) {
      return false;
    }
    if (this.methodDesc == null) {
      if (other.methodDesc != null) {
        return false;
      }
    } else if (!this.methodDesc.equals(other.methodDesc)) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Location [clazz=" + this.clazz + ", method=" + this.method
        + ", methodDesc=" + this.methodDesc + "]";
  }

  public String describe() {
    return this.method.name();
  }

  @Override
  public int compareTo(final Location o) {
    int comp = this.clazz.compareTo(o.getClassName());
    if (comp != 0) {
      return comp;
    }

    comp = this.method.name().compareTo(o.getMethodName().name());
    if (comp != 0) {
      return comp;
    }

    return this.methodDesc.compareTo(o.getMethodDesc());
  }

}
