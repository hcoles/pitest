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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.pitest.ConcreteConfiguration;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.functional.FCollection;

/**
 * @author henry
 * 
 */
public final class TestClass implements Serializable {

  private static final long serialVersionUID = 1L;
  private final Class<?>    clazz;

  public TestClass(final Class<?> clazz) {
    this.clazz = clazz;
  }

  private Collection<TestUnit> getTestUnitsWithinClass(
      final Configuration startConfig, final TestDiscoveryListener listener) {

    final Configuration classConfig = ConcreteConfiguration.updateConfig(
        startConfig, this);

    final TestUnitProcessor applyProcessors = new TestUnitProcessor() {
      public TestUnit apply(final TestUnit tu) {
        TestUnit alteredTestUnit = tu;
        for (final TestUnitProcessor tup : classConfig.testUnitProcessors()) {
          alteredTestUnit = tup.apply(alteredTestUnit);
        }

        return alteredTestUnit;
      }
    };

    final Collection<TestUnit> units = new ArrayList<TestUnit>();

    for (final TestUnitFinder each : classConfig.testUnitFinders()) {
      if (each.canHandle(TestClass.this.getClazz(), !units.isEmpty())) {
        final Collection<TestUnit> newTests = each.findTestUnits(
            TestClass.this, classConfig, listener, applyProcessors);

        units.addAll(newTests);
      }
    }

    return FCollection.map(units, applyProcessors);
  }

  public Class<?> getClazz() {
    return this.clazz;
  }

  public Collection<TestClass> getChildren(final Configuration startConfig) {

    final List<TestClass> children = new ArrayList<TestClass>();
    for (final TestSuiteFinder i : startConfig.testSuiteFinders()) {
      children.addAll(i.apply(this));
    }
    return children;
  }

  private void findTestUnits(final List<TestUnit> tus,
      final TestClass suiteClass, final Configuration startConfig,
      final TestDiscoveryListener listener) {

    listener.enterClass(suiteClass.getClazz());

    final Configuration classConfig = ConcreteConfiguration.updateConfig(
        startConfig, suiteClass);

    final Collection<TestClass> tcs = suiteClass.getChildren(classConfig);
    for (final TestClass tc : tcs) {
      findTestUnits(tus, tc, ConcreteConfiguration
          .updateConfig(classConfig, tc), listener);
    }
    tus.addAll(suiteClass.getTestUnitsWithinClass(startConfig, listener));

    listener.leaveClass(suiteClass.getClazz());

  }

  public Collection<TestUnit> getTestUnits(final Configuration startConfig,
      final TestDiscoveryListener listener) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    findTestUnits(tus, this, startConfig, listener);
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

}