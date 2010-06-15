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
package org.pitest.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;

import org.pitest.functional.predicate.Predicate;

/**
 * @author henry
 * 
 */
public class AnnotatedWithOneOf implements Predicate<Method> {

  private final Set<Class<? extends Annotation>> annotations = new LinkedHashSet<Class<? extends Annotation>>();

  public AnnotatedWithOneOf(final Class<? extends Annotation>... classes) {
    for (final Class<? extends Annotation> a : classes) {
      this.annotations.add(a);
    }
  }

  public Boolean apply(final Method method) {
    for (final Class<? extends Annotation> a : this.annotations) {
      if (method.isAnnotationPresent(a)) {
        return true;
      }
    }
    return false;
  }

}
