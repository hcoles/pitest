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
package org.pitest.mutationtest;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.bcel.classfile.JavaClass;
import org.pitest.Description;
import org.pitest.TestMethod;
import org.pitest.annotations.MutationTest;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Common;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.TestClass;
import org.pitest.reflection.Reflection;

import com.reeltwo.jumble.mutation.Mutater;

public class MutationTestUnitFinder implements TestUnitFinder {

  /**
   * 
   */
  private static final long                   serialVersionUID = 1L;
  private final static MutationTestUnitFinder instance         = new MutationTestUnitFinder();

  public static MutationTestUnitFinder instance() {
    return instance;
  }

  private MutationTestUnitFinder() {

  }

  public boolean canHandle(final boolean alreadyHandled) {
    return alreadyHandled;
  }

  public List<TestUnit> processChildUnits(final List<TestUnit> tus,
      final TestClass testClass) {
    return tus;
  }

  public Collection<TestUnit> findTestUnits(final TestClass testClass,
      final Configuration config) {
    final F<Method, Collection<TestUnit>> f = new F<Method, Collection<TestUnit>>() {
      public Collection<TestUnit> apply(final Method a) {
        return methodToTestUnit(testClass, config, a);
      }

    };
    final List<TestUnit> tus = FCollection.flatMap(
        findAnnotedMethods(testClass), f);
    return FCollection.filter(tus, Common.<TestUnit> fIsNotNull());
  }

  private Collection<Method> findAnnotedMethods(final TestClass testClass) {
    final Predicate<Method> p = new Predicate<Method>() {
      public Boolean apply(final Method method) {
        final org.pitest.annotations.MutationTest annotation = method
            .getAnnotation(org.pitest.annotations.MutationTest.class);
        return (annotation != null);
      }

    };
    return Reflection.publicMethods(testClass.getClazz(), p);
  }

  private List<TestUnit> methodToTestUnit(final TestClass testClass,
      final Configuration configuration, final Method method) {
    try {

      final TestMethod tm = new TestMethod(method, null);
      final org.pitest.annotations.MutationTest annotation = method
          .getAnnotation(org.pitest.annotations.MutationTest.class);
      final MutationConfig config = (MutationConfig) method.invoke(null);
      // final String methodName = method.getName();
      final Mutater m = config.createMutator();

      final String name = annotation.mutateClass().getName();
      final JavaClass unmodifiedClass = m.jumbler(name);
      final int mutationCount = m.countMutationPoints(name);

      if (mutationCount > 0) {
        return createTestUnitGroup(testClass, configuration, annotation, tm, m,
            name, unmodifiedClass, mutationCount);
      } else {
        return null;
      }

    } catch (final Exception ex) {
      throw new RuntimeException(ex);
    }

  }

  private List<TestUnit> createTestUnitGroup(final TestClass testClass,
      final Configuration config,
      final org.pitest.annotations.MutationTest annotation,
      final TestMethod tm, final Mutater m, final String name,
      final JavaClass unmodifiedClass, final int mutationCount)
      throws ClassNotFoundException {

    final Class<?>[] tests = findTestClasses(annotation, testClass);

    final MutationTestResultListener l = new CheckAllTestsPassedResultsListener();
    final Description d = new Description(tm.getName() + "[no mutation]",
        testClass.getClazz(), tm);
    final MutationTestUnit unmutatedTest = new MutationTestUnit(null,
        unmodifiedClass, null, l, tests, d, config);

    final List<TestUnit> tus = createTestUnits(testClass, config, tests, tm, m,
        name, mutationCount, unmutatedTest);

    return tus;
  }

  private Class<?>[] findTestClasses(final MutationTest annotation,
      final TestClass testClass) {
    if (annotation.testClasses()[0]
        .equals(org.pitest.annotations.MutationTest.USE_PARENT.class)) {
      final Class<?>[] tests = new Class<?>[1];
      tests[0] = testClass.getClazz();
      return tests;
    } else {
      return annotation.testClasses();
    }
  }

  private List<TestUnit> createTestUnits(final TestClass testClass,
      final Configuration config, final Class<?>[] testClasses,
      final TestMethod tm, final Mutater m, final String name,
      final int mutationCount, final MutationTestUnit unmutatedTest)
      throws ClassNotFoundException {
    final List<TestUnit> tus = new ArrayList<TestUnit>();
    tus.add(unmutatedTest);
    for (int i = 0; i < mutationCount; i++) {
      m.setMutationPoint(i);
      final JavaClass mutatedClass = m.jumbler(name);
      final MutationDetails details = new MutationDetails(mutatedClass
          .getClassName(), mutatedClass.getFileName(), m.getModification(), m
          .getMutatedMethodName(name));
      final MutationTestResultListener l2 = new CheckTestHasFailedResultListener();
      final Description d2 = new Description(tm.getName() + "[mutation " + i
          + " " + m.getModification() + " in method "
          + m.getMutatedMethodName(name) + "]", testClass.getClazz(), tm);
      final TestUnit tu = new MutationTestUnit(unmutatedTest, mutatedClass,
          details, l2, testClasses, d2, config);

      tus.add(tu);

    }
    return tus;
  }

}
