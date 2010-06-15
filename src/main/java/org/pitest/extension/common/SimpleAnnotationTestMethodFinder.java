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
package org.pitest.extension.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import org.pitest.TestMethod;
import org.pitest.extension.MethodFinder;
import org.pitest.functional.Option;

public class SimpleAnnotationTestMethodFinder implements MethodFinder {

  /**
   * 
   */
  private static final long                 serialVersionUID = 1L;
  private final Class<? extends Annotation> annotationClass;

  public SimpleAnnotationTestMethodFinder(
      final Class<? extends Annotation> annotationClass) {
    this.annotationClass = annotationClass;
  }

  public Option<TestMethod> apply(final Method method) {
    final Annotation a = method.getAnnotation(this.annotationClass);
    if (a != null) {
      return Option.someOrNone(new TestMethod(method, null));
    } else {
      return Option.none();
    }
  }

  public void reset() {
    // TODO Auto-generated method stub

  }

}
