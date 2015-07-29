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

package org.pitest.simpletest;

import java.lang.reflect.Method;

import org.pitest.functional.Option;

public class TestMethod {

  private final Method                     method;
  private final Class<? extends Throwable> expected;

  public TestMethod(final Method method) {
    this(method, null);
  }

  public TestMethod(final Method method,
      final Class<? extends Throwable> expected) {
    this.method = method;
    this.expected = expected;
  }

  public String getName() {
    return this.method.getName();
  }

  public Method getMethod() {
    return this.method;
  }

  public Option<Class<? extends Throwable>> getExpected() {
    return Option.<Class<? extends Throwable>> some(this.expected);
  }

  @Override
  public String toString() {
    return "TestMethod [expected=" + this.expected + ", method=" + this.method
        + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.expected == null) ? 0 : this.expected.hashCode());
    result = (prime * result)
        + ((this.method == null) ? 0 : this.method.hashCode());
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
    final TestMethod other = (TestMethod) obj;
    if (this.expected == null) {
      if (other.expected != null) {
        return false;
      }
    } else if (!this.expected.equals(other.expected)) {
      return false;
    }
    if (this.method == null) {
      if (other.method != null) {
        return false;
      }
    } else if (!this.method.equals(other.method)) {
      return false;
    }
    return true;
  }

}
