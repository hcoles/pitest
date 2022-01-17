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

import java.io.Serializable;
import java.util.Objects;

import org.pitest.classinfo.ClassName;

/**
 * The co-ordinates of a method within a class.
 *
 */
public final class Location implements Comparable<Location>, Serializable  {

  private static final long serialVersionUID = 1L;

  private final ClassName  clazz;
  private final String     method;
  private final String     methodDesc;

  public Location(final ClassName clazz, final String method,
      final String methodDesc) {
    this.clazz = clazz;
    this.method = method;
    this.methodDesc = methodDesc;
  }

  public static Location location(final ClassName clazz,
      final String method, final String methodDesc) {
    return new Location(clazz, method, methodDesc);
  }

  public ClassName getClassName() {
    return this.clazz;
  }

  public String getMethodName() {
    return this.method;
  }

  public String getMethodDesc() {
    return this.methodDesc;
  }

  @Override
  public int hashCode() {
    return Objects.hash(clazz, method, methodDesc);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final Location other = (Location) obj;
    return Objects.equals(clazz, other.clazz)
            && Objects.equals(method, other.method)
            && Objects.equals(methodDesc, other.methodDesc);
  }

  @Override
  public String toString() {
    return "Location [clazz=" + this.clazz + ", method=" + this.method
        + ", methodDesc=" + this.methodDesc + "]";
  }

  public String describe() {
    return this.method;
  }

  @Override
  public int compareTo(final Location o) {
    int comp = this.clazz.compareTo(o.getClassName());
    if (comp != 0) {
      return comp;
    }

    comp = this.method.compareTo(o.getMethodName());
    if (comp != 0) {
      return comp;
    }

    return this.methodDesc.compareTo(o.getMethodDesc());
  }

}
