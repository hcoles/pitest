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
package org.pitest.extension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pitest.functional.F;

/**
 * @author henry
 * 
 */
public final class TestClass {

  private final Class<?> clazz;

  public TestClass(final Class<?> clazz) {
    this.clazz = clazz;
  }

  private List<TestUnit> getTestUnitsWithinClass(final Configuration classConfig) {
    return classConfig.testUnitFinder()
        .findTestUnits(TestClass.this.getClazz());
  }

  public Class<?> getClazz() {
    return this.clazz;
  }

  public Collection<TestUnit> getTestUnits(final Configuration startConfig,
      final GroupingStrategy groupStrategy) {

    final List<TestUnit> tus = new ArrayList<TestUnit>();
    final Set<TestClass> visitedClasses = new HashSet<TestClass>();
    findTestUnits(tus, visitedClasses, this, startConfig, groupStrategy);
    return tus;
  }

  private void findTestUnits(final List<TestUnit> tus,
      final Set<TestClass> visitedClasses, final TestClass suiteClass,
      final Configuration startConfig, final GroupingStrategy groupStrategy) {
    visitedClasses.add(suiteClass);
    final Collection<TestClass> tcs = startConfig.testSuiteFinder().apply(
        suiteClass);

    for (final TestClass tc : tcs) {
      if (!visitedClasses.contains(tc)) {
        findTestUnits(tus, visitedClasses, tc, startConfig, groupStrategy);
      }
    }

    final List<TestUnit> testsInThisClass = suiteClass
        .getTestUnitsWithinClass(startConfig);
    if (!testsInThisClass.isEmpty()) {
      tus.addAll(groupStrategy.group(suiteClass, testsInThisClass));
    }

  }

  @Override
  public String toString() {
    return "TestClass [clazz=" + this.clazz + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result)
        + ((this.clazz == null) ? 0 : this.clazz.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final TestClass other = (TestClass) obj;
    if (this.clazz == null) {
      if (other.clazz != null) {
        return false;
      }
    } else if (!this.clazz.equals(other.clazz)) {
      return false;
    }
    return true;
  }

  public static F<Class<?>, TestClass> classToTestClass() {
    return new F<Class<?>, TestClass>() {
      public TestClass apply(final Class<?> a) {
        return new TestClass(a);
      }
    };
  }

}