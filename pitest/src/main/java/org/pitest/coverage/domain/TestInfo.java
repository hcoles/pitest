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
package org.pitest.coverage.domain;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.Option;

public class TestInfo {

  private final String            name;
  private final int               time;
  private final String            definingClass;
  private final Option<ClassName> testee;

  public TestInfo(final String definingClass, final String name,
      final int time, final Option<ClassName> testee) {
    this.definingClass = definingClass;
    this.name = name;
    this.time = time;
    this.testee = testee;
  }

  public String getName() {
    return this.name;
  }

  public int getTime() {
    return this.time;
  }

  @Override
  public String toString() {
    return this.name;
  }

  public static F<TestInfo, ClassName> toDefiningClassName() {
    return new F<TestInfo, ClassName>() {

      public ClassName apply(final TestInfo a) {
        return new ClassName(a.definingClass);
      }

    };
  }

  public boolean directlyHits(final ClassName targetClass) {
    return this.testee.hasSome() && this.testee.value().equals(targetClass);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.definingClass == null) ? 0 : this.definingClass.hashCode());
    result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
    result = prime * result
        + ((this.testee == null) ? 0 : this.testee.hashCode());
    result = prime * result + this.time;
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
    final TestInfo other = (TestInfo) obj;
    if (this.definingClass == null) {
      if (other.definingClass != null) {
        return false;
      }
    } else if (!this.definingClass.equals(other.definingClass)) {
      return false;
    }
    if (this.name == null) {
      if (other.name != null) {
        return false;
      }
    } else if (!this.name.equals(other.name)) {
      return false;
    }
    if (this.testee == null) {
      if (other.testee != null) {
        return false;
      }
    } else if (!this.testee.equals(other.testee)) {
      return false;
    }
    if (this.time != other.time) {
      return false;
    }
    return true;
  }

}
