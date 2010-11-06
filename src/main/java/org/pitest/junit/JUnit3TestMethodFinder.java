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
import java.lang.reflect.Modifier;

import org.pitest.TestMethod;
import org.pitest.extension.MethodFinder;
import org.pitest.functional.Option;

public class JUnit3TestMethodFinder implements MethodFinder {

  /**
   * 
   */
  private static final long                   serialVersionUID = 1L;
  private final static JUnit3TestMethodFinder instance         = new JUnit3TestMethodFinder();

  public static JUnit3TestMethodFinder instance() {
    return instance;
  }

  public Option<TestMethod> apply(final Method method) {
    if (method.getName().startsWith("test")
        && method.getReturnType().equals(Void.TYPE)
        && (method.getParameterTypes().length == 0) && isPublic(method)
        && extendsTestCase(method.getDeclaringClass())) {
      return Option.some(new TestMethod(method));
    } else {
      return Option.none();
    }
  }

  private boolean isPublic(final Method method) {
    return Modifier.isPublic(method.getModifiers());
  }

  private boolean extendsTestCase(final Class<?> clazz) {
    return junit.framework.TestCase.class.isAssignableFrom(clazz);
  }

}
