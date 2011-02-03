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

package org.pitest.junit;

import java.lang.reflect.Method;

import org.junit.Test;
import org.pitest.TestMethod;
import org.pitest.extension.MethodFinder;
import org.pitest.functional.Option;

public class JUnit4TestMethodFinder implements MethodFinder {

  /**
   * 
   */
  private static final long                   serialVersionUID = 1L;
  private final static JUnit4TestMethodFinder INSTANCE         = new JUnit4TestMethodFinder();

  public static JUnit4TestMethodFinder instance() {
    return INSTANCE;
  }

  public Option<TestMethod> apply(final Method method) {
    final Test annotation = method.getAnnotation(org.junit.Test.class);
    if (annotation != null) {
      final Class<? extends Throwable> expected = !annotation.expected()
          .getName().equals("org.junit.Test$None") ? annotation.expected()
          : null;
      return Option.some(new TestMethod(method, expected));
    } else {
      return Option.none();
    }
  }

}
