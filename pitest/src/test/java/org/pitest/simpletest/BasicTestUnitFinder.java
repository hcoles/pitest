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

package org.pitest.simpletest;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.reflection.Reflection;
import org.pitest.simpletest.steps.CallStep;
import org.pitest.testapi.Description;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;
import org.pitest.util.PitError;

public class BasicTestUnitFinder implements TestUnitFinder {

  private final Set<InstantiationStrategy> instantiationStrategies = new LinkedHashSet<InstantiationStrategy>();
  private final Set<MethodFinder>          testMethodFinders       = new LinkedHashSet<MethodFinder>();

  public BasicTestUnitFinder(
      final Collection<InstantiationStrategy> instantiationStrategies,
      final Collection<MethodFinder> testMethodFinders) {
    this.instantiationStrategies.addAll(instantiationStrategies);
    this.testMethodFinders.addAll(testMethodFinders);
  }

  @Override
  public List<TestUnit> findTestUnits(final Class<?> testClass) {
    try {

      final List<TestUnit> units = new ArrayList<TestUnit>();
      final InstantiationStrategy instantiationStrategy = findInstantiationStrategy(testClass);
      final List<TestStep> instantiations = instantiationStrategy
          .instantiations(testClass);
      for (int instantiation = 0; instantiation != instantiations.size(); instantiation++) {
        for (final TestMethod m : findTestMethods(testClass)) {
          final TestStep step = instantiations.get(instantiation);
          units
          .add(createTestUnitForInstantiation(step,
              getNamePrefix(instantiations.size(), instantiation),
              testClass, m));
        }
      }

      return units;

    } catch (final Exception ex) {
      throw translateCheckedException(ex);
    }
  }

  private String getNamePrefix(final int size, final int i) {
    if (size == 1) {
      return "";
    } else {
      return "[" + i + "] ";
    }
  }

  private TestUnit createTestUnitForInstantiation(
      final TestStep instantiationStep, final String namePrefix,
      final Class<?> testClass, final TestMethod testMethod) {

    final List<TestStep> steps = new ArrayList<TestStep>();

    steps.add(instantiationStep);
    steps.add(new CallStep(testMethod));

    final TestUnit unit = new SteppedTestUnit(new Description(namePrefix
        + testMethod.getName(), testClass), steps, testMethod.getExpected());
    return unit;

  }

  private InstantiationStrategy findInstantiationStrategy(final Class<?> clazz) {
    final List<InstantiationStrategy> strategies = FCollection.filter(
        this.instantiationStrategies, canInstantiate(clazz));
    if (strategies.isEmpty()) {
      throw new PitError("Cannot instantiate " + clazz);
    } else {
      return strategies.get(0);
    }
  }

  private F<InstantiationStrategy, Boolean> canInstantiate(final Class<?> clazz) {
    return new F<InstantiationStrategy, Boolean>() {

      @Override
      public Boolean apply(final InstantiationStrategy a) {
        return a.canInstantiate(clazz);
      }

    };
  }

  private Collection<TestMethod> findTestMethods(final Class<?> clazz) {

    final EqualitySet<TestMethod> set = new EqualitySet<TestMethod>(
        new SignatureEqualityStrategy());
    final SideEffect1<TestMethod> addToSet = new SideEffect1<TestMethod>() {
      @Override
      public void apply(final TestMethod a) {
        set.add(a);
      }
    };
    final Collection<Method> methods = Reflection.allMethods(clazz);
    for (final F<Method, Option<TestMethod>> mf : this.testMethodFinders) {
      FCollection.flatMap(methods, mf).forEach(addToSet);
    }

    return set.toCollection();
  }

}
