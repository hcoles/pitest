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

import static org.pitest.util.Unchecked.translateCheckedException;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pitest.Description;
import org.pitest.MultipleTestGroup;
import org.pitest.PitError;
import org.pitest.TestMethod;
import org.pitest.extension.Configuration;
import org.pitest.extension.InstantiationStrategy;
import org.pitest.extension.MethodFinder;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestStep;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.EqualitySet;
import org.pitest.internal.SignatureEqualityStrategy;
import org.pitest.internal.TestClass;
import org.pitest.reflection.Reflection;
import org.pitest.teststeps.CallStep;
import org.pitest.testunit.SteppedTestUnit;

public class BasicTestUnitFinder implements TestUnitFinder {

  private final Set<InstantiationStrategy> instantiationStrategies = new LinkedHashSet<InstantiationStrategy>();
  private final Set<MethodFinder>          testMethodFinders       = new LinkedHashSet<MethodFinder>();
  private final Set<MethodFinder>          beforeMethodFinders     = new LinkedHashSet<MethodFinder>();
  private final Set<MethodFinder>          afterMethodFinders      = new LinkedHashSet<MethodFinder>();
  private final Set<MethodFinder>          beforeClassFinders      = new LinkedHashSet<MethodFinder>();
  private final Set<MethodFinder>          afterClassFinders       = new LinkedHashSet<MethodFinder>();

  public BasicTestUnitFinder(
      final Collection<InstantiationStrategy> instantiationStrategies,
      final Collection<MethodFinder> testMethodFinders,
      final Collection<MethodFinder> beforeMethodFinders,
      final Collection<MethodFinder> afterMethodFinders,
      final Collection<MethodFinder> beforeClassFinders,
      final Collection<MethodFinder> afterClassFinders) {
    this.instantiationStrategies.addAll(instantiationStrategies);
    this.testMethodFinders.addAll(testMethodFinders);
    this.beforeMethodFinders.addAll(beforeMethodFinders);
    this.afterMethodFinders.addAll(afterMethodFinders);
    this.beforeClassFinders.addAll(beforeClassFinders);
    this.afterClassFinders.addAll(afterClassFinders);
  }

  public Collection<TestUnit> findTestUnits(final TestClass testClass,
      final Configuration config, final TestDiscoveryListener listener,
      final TestUnitProcessor processor) {
    try {

      final Collection<TestMethod> befores = findTestMethods(
          this.beforeMethodFinders, testClass.getClazz());
      final Collection<TestMethod> afters = findTestMethods(
          this.afterMethodFinders, testClass.getClazz());

      final List<TestUnit> units = new ArrayList<TestUnit>();
      final InstantiationStrategy instantiationStrategy = findInstantiationStrategy(
          config, testClass.getClazz());
      final List<TestStep> instantiations = instantiationStrategy
          .instantiations(testClass.getClazz());
      for (int instantiation = 0; instantiation != instantiations.size(); instantiation++) {
        for (final TestMethod m : findTestMethods(this.testMethodFinders,
            testClass.getClazz())) {
          final TestStep step = instantiations.get(instantiation);
          units.add(createTestUnitForInstantiation(step,
              getNamePrefix(instantiations.size(), instantiation), befores,
              afters, testClass, config, m));
        }
      }

      listener.receiveTests(units);

      return this.createGroupings(FCollection.map(units, processor), testClass);

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

  private Collection<CallStep> findMethodCalls(
      final Collection<MethodFinder> finders, final Class<?> testClass) {

    final Collection<TestMethod> methods = findTestMethods(finders, testClass);
    return FCollection.map(methods, methodToCallStep());

  }

  private F<TestMethod, CallStep> methodToCallStep() {
    final F<TestMethod, CallStep> f = new F<TestMethod, CallStep>() {
      public CallStep apply(final TestMethod m) {
        return new CallStep(m);
      }
    };
    return f;
  }

  private TestUnit createTestUnitForInstantiation(
      final TestStep instantiationStep, final String namePrefix,
      final Collection<TestMethod> befores,
      final Collection<TestMethod> afters, final TestClass testClass,
      final Configuration config, final TestMethod testMethod) {

    final List<TestStep> steps = new ArrayList<TestStep>();

    steps.add(instantiationStep);

    for (final TestMethod each : befores) {
      steps.add(new CallStep(each));
    }

    steps.add(new CallStep(testMethod));

    for (final TestMethod each : afters) {
      steps.add(new CallStep(each));
    }

    final TestUnit unit = new SteppedTestUnit(new Description(namePrefix
        + testMethod.getName(), testClass.getClazz(), testMethod), steps,
        testMethod.getExpected());
    return unit;

  }

  private InstantiationStrategy findInstantiationStrategy(
      final Configuration config, final Class<?> clazz) {
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

      public Boolean apply(final InstantiationStrategy a) {
        return a.canInstantiate(clazz);
      }

    };
  }

  private Collection<TestMethod> findTestMethods(
      final Collection<MethodFinder> finders, final Class<?> clazz) {

    final EqualitySet<TestMethod> set = new EqualitySet<TestMethod>(
        new SignatureEqualityStrategy());
    final SideEffect1<TestMethod> addToSet = new SideEffect1<TestMethod>() {
      public void apply(final TestMethod a) {
        set.add(a);
      }
    };
    final Collection<Method> methods = Reflection.allMethods(clazz);
    for (final F<Method, Option<TestMethod>> mf : finders) {
      FCollection.flatMap(methods, mf).forEach(addToSet);
    }

    return set.toCollection();
  }

  private List<TestUnit> createGroupings(final List<TestUnit> tus,
      final TestClass testClass) {
    final Collection<CallStep> beforeClasses = findMethodCalls(
        this.beforeClassFinders, testClass.getClazz());

    final Collection<CallStep> afterClasses = findMethodCalls(
        this.afterClassFinders, testClass.getClazz());

    if (!beforeClasses.isEmpty() || (!afterClasses.isEmpty() && !tus.isEmpty())) {
      final TestUnit group = new MultipleTestGroup(tus);
      final TestUnit decorated = new BeforeAfterDecorator(group, beforeClasses,
          afterClasses);
      return Collections.<TestUnit> singletonList(decorated);
    } else {
      return tus;
    }
  }

}
