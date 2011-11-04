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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.ConcreteConfiguration;
import org.pitest.extension.Configuration;
import org.pitest.extension.GroupingStrategy;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitProcessor;
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

  private Collection<TestUnit> getTestUnitsWithinClass(
      final Configuration startConfig, final TestDiscoveryListener listener) {

    final Configuration classConfig = ConcreteConfiguration.updateConfig(
        startConfig, this);

    final Collection<TestUnit> units = findTestUnitsUsingAllTestFinders(
        listener, classConfig, classConfig.testUnitProcessor());

    return units;
  }

  private Collection<TestUnit> findTestUnitsUsingAllTestFinders(
      final TestDiscoveryListener listener, final Configuration classConfig,
      final TestUnitProcessor applyProcessors) {
    final Collection<TestUnit> units = new ArrayList<TestUnit>();
    units.addAll(classConfig.testUnitFinder().findTestUnits(
        TestClass.this.getClazz(), classConfig, listener, applyProcessors));

    return units;
  }

  public Class<?> getClazz() {
    return this.clazz;
  }

  private void findTestUnits(final List<TestUnit> tus,
      final TestClass suiteClass, final Configuration startConfig,
      final GroupingStrategy groupStrategy, final TestDiscoveryListener listener) {

    listener.enterClass(suiteClass.getClazz());

    final Configuration classConfig = ConcreteConfiguration.updateConfig(
        startConfig, suiteClass);

    final Collection<TestClass> tcs = classConfig.testSuiteFinder().apply(
        suiteClass);
    for (final TestClass tc : tcs) {
      findTestUnits(tus, tc,
          ConcreteConfiguration.updateConfig(classConfig, tc), groupStrategy,
          listener);
    }
    final Collection<TestUnit> testsInThisClass = suiteClass
        .getTestUnitsWithinClass(startConfig, listener);
    if (!testsInThisClass.isEmpty()) {
      tus.addAll(groupStrategy.group(suiteClass, testsInThisClass));
    }

    listener.leaveClass(suiteClass.getClazz());

  }

  public Collection<TestUnit> getTestUnits(final Configuration startConfig,
      final TestDiscoveryListener listener, final GroupingStrategy groupStrategy) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    findTestUnits(tus, this, startConfig, groupStrategy, listener);
    return tus;
  }

  @Override
  public String toString() {
    return "TestClass [clazz=" + this.clazz + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
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