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
package org.pitest.testng;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;

import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Prelude;
import org.pitest.functional.predicate.Predicate;
import org.pitest.reflection.IsAnotatedWith;
import org.pitest.reflection.Reflection;

public class TestNGTestUnitFinder implements TestUnitFinder {

  public Collection<TestUnit> findTestUnits(final Class<?> clazz) {
    if (hasClassAnnotation(clazz)) {
      return findForAnnotatedClazz(clazz);
    } else if (hasMethodAnnotation(clazz)) {
      return findForAnnotateMethods(clazz);
    } else {
      return Collections.emptyList();
    }

  }

  @SuppressWarnings("unchecked")
  private Collection<TestUnit> findForAnnotatedClazz(Class<?> clazz) {
    return FCollection.map(Reflection.publicMethods(clazz, Prelude.or(
        IsAnotatedWith.instance(org.testng.annotations.Test.class),
        notDefinedByObject())), methodToTestUnit(clazz));
  }

  private Predicate<Method> notDefinedByObject() {
    return new Predicate<Method>() {

      public Boolean apply(Method a) {
        return a.getDeclaringClass() != Object.class;
      }

    };
  }

  private Collection<TestUnit> findForAnnotateMethods(Class<?> clazz) {
    return FCollection.map(
        Reflection.publicMethods(clazz,
            IsAnotatedWith.instance(org.testng.annotations.Test.class)),
        methodToTestUnit(clazz));
  }

  private static F<Method, TestUnit> methodToTestUnit(final Class<?> clazz) {
    return new F<Method, TestUnit>() {

      public TestUnit apply(Method a) {
        return new TestNGTestUnit(clazz, a.getName());
      }

    };
  }

  private boolean hasClassAnnotation(final Class<?> clazz) {
    return clazz.getAnnotation(org.testng.annotations.Test.class) != null;

  }

  private boolean hasMethodAnnotation(final Class<?> clazz) {
    return FCollection.contains(Reflection.allMethods(clazz),
        IsAnotatedWith.instance(org.testng.annotations.Test.class));
  }

}
