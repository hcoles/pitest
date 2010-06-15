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
import java.lang.reflect.Method;

public class TestMethod implements Serializable {

  private static final long                serialVersionUID = 1L;

  private final JavaMethod                 method;
  private final Class<? extends Throwable> expected;

  public TestMethod(final Method method,
      final Class<? extends Throwable> expected) {
    this.method = new JavaMethod(method);
    this.expected = expected;
  }

  public String getName() {
    return this.method.method().getName();
  }

  public Method getMethod() {
    return this.method.method();
  }

  public Class<? extends Throwable> getExpected() {
    return this.expected;
  }

  @Override
  public String toString() {
    return "TestMethod [expected=" + this.expected + ", method=" + this.method
        + "]";
  }

}
