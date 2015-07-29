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

package org.pitest.coverage;

import org.pitest.classinfo.ClassName;

public final class ClassLine {
  private final ClassName clazz;
  private final int       lineNumber;

  public ClassLine(final String clazz, final int lineNumber) {
    this(ClassName.fromString(clazz), lineNumber);
  }

  public ClassLine(final ClassName clazz, final int lineNumber) {
    this.clazz = clazz;
    this.lineNumber = lineNumber;
  }

  public ClassName getClassName() {
    return this.clazz;
  }

  public int getLineNumber() {
    return this.lineNumber;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.clazz == null) ? 0 : this.clazz.hashCode());
    result = (prime * result) + this.lineNumber;
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
    final ClassLine other = (ClassLine) obj;
    if (this.clazz == null) {
      if (other.clazz != null) {
        return false;
      }
    } else if (!this.clazz.equals(other.clazz)) {
      return false;
    }
    if (this.lineNumber != other.lineNumber) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "ClassLine [" + this.clazz + ":" + this.lineNumber + "]";
  }

}