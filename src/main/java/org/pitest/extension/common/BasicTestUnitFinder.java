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
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.pitest.Description;
import org.pitest.TestMethod;
import org.pitest.extension.Configuration;
import org.pitest.extension.MethodFinder;
import org.pitest.extension.TestStep;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.EqualitySet;
import org.pitest.internal.SignatureEqualityStrategy;
import org.pitest.internal.TestClass;
import org.pitest.internal.TestMethodInstantiationPair;
import org.pitest.reflection.Reflection;
import org.pitest.teststeps.CallStep;
import org.pitest.testunit.SteppedTestUnit;

public class BasicTestUnitFinder implements TestUnitFinder {

  private final Set<MethodFinder> testMethodFinders   = new LinkedHashSet<MethodFinder>();
  private final Set<MethodFinder> beforeMethodFinders = new LinkedHashSet<MethodFinder>();
  private final Set<MethodFinder> afterMethodFinders  = new LinkedHashSet<MethodFinder>();
  private final Set<MethodFinder> beforeClassFinders  = new LinkedHashSet<MethodFinder>();
  private final Set<MethodFinder> afterClassFinders   = new LinkedHashSet<MethodFinder>();

  public BasicTestUnitFinder(final Set<MethodFinder> testMethodFinders,
      final Set<MethodFinder> beforeMethodFinders,
      final Set<MethodFinder> afterMethodFinders,
      final Set<MethodFinder> beforeClassFinders,
      final Set<MethodFinder> afterClassFinders) {
    this.testMethodFinders.addAll(testMethodFinders);
    this.beforeMethodFinders.addAll(beforeMethodFinders);
    this.afterMethodFinders.addAll(afterMethodFinders);
    this.beforeClassFinders.addAll(beforeClassFinders);
    this.afterClassFinders.addAll(afterClassFinders);
  }

  public Collection<TestUnit> apply(final TestClass a,
      final Configuration config) {
    try {

      final Collection<TestMethod> befores = findTestMethods(
          this.beforeMethodFinders, a.getClazz());
      final Collection<TestMethod> afters = findTestMethods(
          this.afterMethodFinders, a.getClazz());
      final Collection<TestMethodInstantiationPair> tests = findTestMethodsInstantiations(
          this.testMethodFinders, a, config);

      final List<TestUnit> units = createClassTestUnits(befores, afters, tests,
          a, config);

      dependOnFirst(units);

      return units;

    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }
  }

  private Collection<CallStep> findMethodCalls(
      final Collection<MethodFinder> finders, final Class<?> testClass) {

    final Collection<TestMethod> methods = findTestMethods(finders, testClass);
    final F<TestMethod, CallStep> f = new F<TestMethod, CallStep>() {

      public CallStep apply(final TestMethod m) {
        return new CallStep(m);
      }

    };
    return FCollection.map(methods, f);

  }

  private List<TestUnit> createClassTestUnits(
      final Collection<TestMethod> befores,
      final Collection<TestMethod> afters,
      final Collection<TestMethodInstantiationPair> tests,
      final TestClass testClass, final Configuration config) {
    final F<TestMethodInstantiationPair, TestUnit> fMethodToTestUnit = new F<TestMethodInstantiationPair, TestUnit>() {
      public TestUnit apply(final TestMethodInstantiationPair m) {
        return createTestUnitForInstantiation(m, befores, afters, testClass,
            config);
      }

    };

    final List<TestUnit> units = FCollection.map(tests, fMethodToTestUnit);
    return units;
  }

  private TestUnit createTestUnitForInstantiation(
      final TestMethodInstantiationPair m,
      final Collection<TestMethod> befores,
      final Collection<TestMethod> afters, final TestClass testClass,
      final Configuration config) {

    final List<TestStep> steps = new ArrayList<TestStep>();

    steps.add(m.getInstantiation());

    for (final TestMethod each : befores) {
      steps.add(new CallStep(each));
    }

    steps.add(new CallStep(m.getMethod()));

    for (final TestMethod each : afters) {
      steps.add(new CallStep(each));
    }

    final TestUnit unit = new SteppedTestUnit(new Description(m.getMethod()
        .getName(), testClass.getClazz(), m.getMethod()), steps, m.getMethod()
        .getExpected());
    return unit;

  }

  private Collection<TestMethodInstantiationPair> findTestMethodsInstantiations(
      final Set<MethodFinder> finders, final TestClass clazz,
      final Configuration config) {

    final List<TestMethodInstantiationPair> values = new ArrayList<TestMethodInstantiationPair>();
    for (final TestStep i : config.instantiationStrategy().instantiations(
        clazz.getClazz())) {
      for (final TestMethod m : findTestMethods(finders, clazz.getClazz())) {
        values.add(new TestMethodInstantiationPair(i, m));
      }
    }
    return values;
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
      FCollection.flatMap(methods, mf, addToSet);
    }

    return set.toCollection();
  }

  public List<TestUnit> processChildUnits(final List<TestUnit> tus,
      final TestClass testClass) {

    final Collection<CallStep> beforeClasses = findMethodCalls(
        this.beforeClassFinders, testClass.getClazz());

    final Collection<CallStep> afterClasses = findMethodCalls(
        this.afterClassFinders, testClass.getClazz());

    if (!beforeClasses.isEmpty() || (!afterClasses.isEmpty() && !tus.isEmpty())) {

      final TestUnit first = tus.get(0);
      tus.set(0, new BeforeAfterDecorator(first, beforeClasses, Collections
          .<CallStep> emptySet()));
      final TestUnit last = tus.get(tus.size() - 1);
      tus.set(tus.size() - 1, new BeforeAfterDecorator(last, Collections
          .<CallStep> emptySet(), afterClasses));
      chainDepencies(tus);
    }

    return tus;

  }

  private void chainDepencies(final List<TestUnit> tus) {
    for (int i = 0; i != tus.size(); i++) {
      if (i != 0) {
        tus.get(i).setDependency(tus.get(i - 1));
      }
    }
  }

  private void dependOnFirst(final List<TestUnit> tus) {
    if (tus.size() >= 1) {
      final TestUnit firstUnit = tus.get(0);
      final SideEffect1<TestUnit> e = new SideEffect1<TestUnit>() {
        public void apply(final TestUnit a) {
          a.setDependency(firstUnit);
        }
      };
      FCollection.forEach(tus.subList(1, tus.size()), e);
    }
  }

}
