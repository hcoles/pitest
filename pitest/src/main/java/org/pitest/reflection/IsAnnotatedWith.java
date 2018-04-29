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
import java.lang.reflect.AccessibleObject;
import java.util.function.Predicate;

/**
 * @author henry
 *
 */
public class IsAnnotatedWith implements Predicate<AccessibleObject> {

  private final Class<? extends Annotation> clazz;

  public static IsAnnotatedWith instance(final Class<? extends Annotation> clazz) {
    return new IsAnnotatedWith(clazz);
  }

  public IsAnnotatedWith(final Class<? extends Annotation> clazz) {
    this.clazz = clazz;
  }

  @Override
  public boolean test(final AccessibleObject a) {
    return a.isAnnotationPresent(this.clazz);
  }

}
