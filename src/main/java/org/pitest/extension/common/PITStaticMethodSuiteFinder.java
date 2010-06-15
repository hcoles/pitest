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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.pitest.annotations.PITSuiteMethod;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.internal.TestClass;
import org.pitest.reflection.IsAnotatedWith;
import org.pitest.reflection.Reflection;

public class PITStaticMethodSuiteFinder implements TestSuiteFinder {

  @SuppressWarnings("unchecked")
  public Collection<TestClass> apply(final TestClass root) {
    final List<TestClass> result = new ArrayList<TestClass>();

    try {
      final F<Class<?>, TestClass> fClassToTestClass = new F<Class<?>, TestClass>() {
        public TestClass apply(final Class<?> a) {
          return new TestClass(a);
        }
      };

      final Set<Method> suites = Reflection.publicMethods(root.getClazz(),
          IsAnotatedWith.instance(PITSuiteMethod.class));
      for (final Method suiteMethod : suites) {
        final Collection<Class<?>> testClasses = (Collection<Class<?>>) suiteMethod
            .invoke(null);
        FCollection.map(testClasses, fClassToTestClass, result);
      }

      return result;

    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

}
