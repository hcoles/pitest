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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.pitest.extension.InstantiationStrategy;
import org.pitest.extension.TestStep;
import org.pitest.functional.predicate.And;
import org.pitest.reflection.IsAnotatedWith;
import org.pitest.reflection.IsStatic;
import org.pitest.reflection.Reflection;
import org.pitest.teststeps.ParameterisedInstantiationStep;

public class ParameterizedInstantiationStrategy implements
    InstantiationStrategy {

  public boolean canInstantiate(final Class<?> clazz) {
    final RunWith runWith = clazz.getAnnotation(RunWith.class);
    if (runWith != null) {
      if (runWith.value().equals(Parameterized.class)) {
        return true;
      }

      if (runWith.value().equals(PITJUnitRunner.class)) {
        return !getParametersMethods(clazz).isEmpty();
      }
    }
    return false;
  }

  public List<TestStep> instantiations(final Class<?> clazz) {
    final List<Object[]> parameters = getParametersList(clazz);
    final List<TestStep> instantiations = new ArrayList<TestStep>();
    for (final Object[] each : parameters) {
      instantiations.add(new ParameterisedInstantiationStep(clazz, each));
    }
    return instantiations;
  }

  @SuppressWarnings("unchecked")
  private List<Object[]> getParametersList(final Class<?> clazz) {
    try {
      return (List<Object[]>) getParametersMethod(clazz).invoke(null);
    } catch (final Exception e) {
      throw new RuntimeException(e);
    }
  }

  private Method getParametersMethod(final Class<?> clazz) {
    final Set<Method> methods = getParametersMethods(clazz);
    if (methods.isEmpty()) {
      throw new RuntimeException(
          "No public static method annotated with Parameters on class "
              + clazz.getName());
    } else {
      return methods.iterator().next();
    }
  }

  @SuppressWarnings("unchecked")
  private Set<Method> getParametersMethods(final Class<?> clazz) {
    final Set<Method> methods = Reflection.publicMethods(clazz, And.instance(
        IsStatic.instance(), IsAnotatedWith.instance(Parameters.class)));
    return methods;
  }

}
