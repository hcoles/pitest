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

import org.pitest.extension.Configuration;
import org.pitest.extension.TestSuiteFinder;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.functional.F;
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

  public Collection<TestUnit> getTestUnitsWithinClass(final Configuration config) {

    final F<TestUnit, TestUnit> applyProcessors = new F<TestUnit, TestUnit>() {
      public TestUnit apply(final TestUnit tu) {
        TestUnit alteredTestUnit = tu;
        for (final TestUnitProcessor tup : config.testUnitProcessors()) {
          alteredTestUnit = tup.apply(alteredTestUnit);
        }

        return alteredTestUnit;
      }
    };

    final F<TestUnitFinder, Collection<TestUnit>> f = new F<TestUnitFinder, Collection<TestUnit>>() {
      public Collection<TestUnit> apply(final TestUnitFinder a) {
        return a.apply(TestClass.this, config);
      }
    };
    final Collection<TestUnit> units = FCollection.flatMap(config
        .testUnitFinders(), f);

    // return units;
    return FCollection.map(units, applyProcessors);
  }

  public Class<?> getClazz() {
    return this.clazz;
  }

  public Collection<TestClass> getChildren(final Configuration config) {
    final List<TestClass> children = new ArrayList<TestClass>();
    for (final TestSuiteFinder i : config.testSuiteFinders()) {
      children.addAll(i.apply(this));
    }
    return children;
  }

  private void findTestUnits(final List<TestUnit> tus,
      final TestClass suiteClass, final Configuration config) {

    final Collection<TestClass> tcs = suiteClass.getChildren(config);
    for (final TestClass tc : tcs) {
      findTestUnits(tus, tc, config);
    }
    tus.addAll(suiteClass.getTestUnitsWithinClass(config));
  }

  public Collection<TestUnit> getTestUnits(final Configuration config) {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    findTestUnits(tus, this, config);
    List<TestUnit> modifiedTus = tus;
    for (final TestUnitFinder each : config.testUnitFinders()) {
      modifiedTus = each.processChildUnits(modifiedTus, this);
    }

    return modifiedTus;

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
