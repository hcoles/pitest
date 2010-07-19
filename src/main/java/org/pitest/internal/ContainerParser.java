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

package org.pitest.internal;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.lang.reflect.Method;
import java.util.Set;

import org.pitest.annotations.PIT;
import org.pitest.annotations.PITContainer;
import org.pitest.extension.Container;
import org.pitest.functional.Option;
import org.pitest.reflection.IsAnotatedWith;
import org.pitest.reflection.Reflection;

public class ContainerParser {

  private final Class<?> clazz;

  public ContainerParser(final Class<?> clazz) {
    this.clazz = clazz;
  }

  public Container create(final Container defaultContainer) {

    final Container c = determineContainer().getOrElse(defaultContainer);
    if (determineNoThreads().hasSome()) {
      c.setMaxThreads(determineNoThreads().value());
    }

    return c;

  }

  private Option<Integer> determineNoThreads() {
    if (this.clazz.isAnnotationPresent(PIT.class)) {
      return Option.someOrNone(this.clazz.getAnnotation(PIT.class)
          .maxParallel());
    } else {
      return Option.none();
    }
  }

  private Option<Container> determineContainer() {
    final Set<Method> containerMethod = Reflection.publicMethods(this.clazz,
        IsAnotatedWith.instance(PITContainer.class));
    try {
      if (!containerMethod.isEmpty()) {
        final Container c = (Container) containerMethod.iterator().next()
            .invoke(null);
        return Option.someOrNone(c);
      } else {
        return Option.none();
      }

    } catch (final Exception e) {
      throw translateCheckedException(e);
    }
  }

}
