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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.pitest.MultipleTestGroup;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.internal.TestClass;
import org.pitest.reflection.Reflection;

public class CustomJUnit3TestUnitFinder implements TestUnitFinder {

  public boolean canHandle(final Class<?> clazz, final boolean alreadyHandled) {
    return isCustomJUnit3Class(clazz);
  }

  public Collection<TestUnit> findTestUnits(final TestClass a,
      final Configuration b, final TestDiscoveryListener listener) {

    if (isCustomJUnit3Class(a.getClazz())) {
      final RunnerAdapter adapter = new RunnerAdapter(a.getClazz());
      final List<TestUnit> units = adapter.getTestUnits();
      listener.reciveTests(units);
      return Collections.<TestUnit> singletonList(new MultipleTestGroup(units));
    } else {
      return Collections.emptyList();
    }

  }

  public static boolean isCustomJUnit3Class(final Class<?> a) {
    // treat junit three classes that override lifecycle methods as custom
    if (TestCase.class.isAssignableFrom(a)) {
      final Method runBareMethod = Reflection.publicMethod(a, "runBare");
      return runBareMethod.getDeclaringClass() != TestCase.class;
    } else {
      return false;
    }
  }

}
