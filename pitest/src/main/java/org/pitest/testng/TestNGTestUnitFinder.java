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

import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;

import org.pitest.functional.FCollection;
import org.pitest.reflection.IsAnnotatedWith;
import org.pitest.reflection.Reflection;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;

public class TestNGTestUnitFinder implements TestUnitFinder {

  private final TestGroupConfig config;

  public TestNGTestUnitFinder(final TestGroupConfig config) {
    this.config = config;
  }

  @Override
  public List<TestUnit> findTestUnits(final Class<?> clazz) {

    if (!isAbstract(clazz) && (hasClassAnnotation(clazz) || hasMethodAnnotation(clazz))) {
      return Collections.<TestUnit> singletonList(new TestNGTestUnit(clazz,
          this.config));
    }
    return Collections.emptyList();

  }

  private boolean hasClassAnnotation(final Class<?> clazz) {
    return clazz.getAnnotation(org.testng.annotations.Test.class) != null;

  }

  private boolean hasMethodAnnotation(final Class<?> clazz) {
    return FCollection.contains(Reflection.allMethods(clazz),
        IsAnnotatedWith.instance(org.testng.annotations.Test.class));
  }

  private boolean isAbstract(Class<?> clazz) {
    return Modifier.isAbstract(clazz.getModifiers());
  }

}
