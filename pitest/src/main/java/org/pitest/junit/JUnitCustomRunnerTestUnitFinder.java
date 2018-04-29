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

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runners.Parameterized;
import org.pitest.functional.FCollection;
import org.pitest.junit.adapter.AdaptedJUnitTestUnit;
import org.pitest.reflection.IsAnnotatedWith;
import org.pitest.reflection.Reflection;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;
import org.pitest.util.IsolationUtils;
import org.pitest.util.Preconditions;

public class JUnitCustomRunnerTestUnitFinder implements TestUnitFinder {

  @SuppressWarnings("rawtypes")
  private static final Optional<Class> CLASS_RULE = findClassRuleClass();


  private final TestGroupConfig config;
  private final Collection<String> excludedRunners;
  private final Collection<String> includedTestMethods;

  JUnitCustomRunnerTestUnitFinder(TestGroupConfig config, final Collection<String> excludedRunners,
                                  final Collection<String> includedTestMethods) {
    Preconditions.checkNotNull(config);
    this.config = config;
    this.excludedRunners = excludedRunners;
    this.includedTestMethods = includedTestMethods;
  }

  @Override
  public List<TestUnit> findTestUnits(final Class<?> clazz) {

    final Runner runner = AdaptedJUnitTestUnit.createRunner(clazz);

    if (isExcluded(runner) || isNotARunnableTest(runner, clazz.getName()) || !isIncluded(clazz)) {
      return Collections.emptyList();
    }

    if (Filterable.class.isAssignableFrom(runner.getClass())
        && !shouldTreatAsOneUnit(clazz, runner)) {
      final List<TestUnit> filteredUnits = splitIntoFilteredUnits(runner.getDescription());
      return filterUnitsByMethod(filteredUnits);
    } else {
      return Collections.<TestUnit> singletonList(new AdaptedJUnitTestUnit(
          clazz, Optional.<Filter> empty()));
    }
  }

  private List<TestUnit> filterUnitsByMethod(List<TestUnit> filteredUnits) {
    if (this.includedTestMethods.isEmpty()) {
      return filteredUnits;
    }

    final List<TestUnit> units = new ArrayList<>();
    for (final TestUnit unit: filteredUnits) {
      if (this.includedTestMethods.contains(unit.getDescription().getName().split("\\(")[0])) {
        units.add(unit);
      }
    }
    return units;
  }

  private boolean isExcluded(Runner runner) {
    return this.excludedRunners.contains(runner.getClass().getName());
  }

  private boolean isIncluded(final Class<?> a) {
    return isIncludedCategory(a) && !isExcludedCategory(a);
  }


  private boolean isIncludedCategory(final Class<?> a) {
    final List<String> included = this.config.getIncludedGroups();
    return included.isEmpty() || !Collections.disjoint(included, getCategories(a));
  }

  private boolean isExcludedCategory(final Class<?> a) {
    final List<String> excluded = this.config.getExcludedGroups();
    return !excluded.isEmpty() && !Collections.disjoint(excluded, getCategories(a));
  }

  private List<String> getCategories(final Class<?> a) {
    final Category c = a.getAnnotation(Category.class);
    return FCollection.flatMap(Arrays.asList(c), toCategoryNames());
  }

  private Function<Category, Iterable<String>> toCategoryNames() {
    return a -> {
      if (a == null) {
        return Collections.emptyList();
      }
      return FCollection.map(Arrays.asList(a.value()),toName());
    };
  }

  private Function<Class<?>,String> toName() {
    return a -> a.getName();
  }

  private boolean isNotARunnableTest(final Runner runner,
      final String className) {
    try {
      return (runner == null)
          || runner.getClass().isAssignableFrom(ErrorReportingRunner.class)
          || isParameterizedTest(runner)
          || isAJUnitThreeErrorOrWarning(runner)
          || isJUnitThreeSuiteMethodNotForOwnClass(runner, className);
    } catch (final RuntimeException ex) {
      // some runners (looking at you spock) can throw a runtime exception
      // when the getDescription method is called
      return true;
    }
  }

  private boolean isAJUnitThreeErrorOrWarning(final Runner runner) {
    return !runner.getDescription().getChildren().isEmpty()
        && runner.getDescription().getChildren().get(0).getClassName()
            .startsWith("junit.framework.TestSuite");
  }

  private boolean shouldTreatAsOneUnit(final Class<?> clazz, final Runner runner) {
    final Set<Method> methods = Reflection.allMethods(clazz);
    return runnerCannotBeSplit(runner)
        || hasAnnotation(methods, BeforeClass.class)
        || hasAnnotation(methods, AfterClass.class)
        || hasClassRuleAnnotations(clazz, methods);
  }

  private boolean hasClassRuleAnnotations(final Class<?> clazz,
      final Set<Method> methods) {
    if (!CLASS_RULE.isPresent()) {
      return false;
    }

    return hasAnnotation(methods, CLASS_RULE.get())
        || hasAnnotation(Reflection.publicFields(clazz), CLASS_RULE.get());
  }

  private boolean hasAnnotation(final Set<? extends AccessibleObject> methods,
      final Class<? extends Annotation> annotation) {
    return FCollection.contains(methods, IsAnnotatedWith.instance(annotation));
  }

  private boolean isParameterizedTest(final Runner runner) {
    return Parameterized.class.isAssignableFrom(runner.getClass());
  }

  private boolean runnerCannotBeSplit(final Runner runner) {
    final String runnerName = runner.getClass().getName();
    return runnerName.equals("junitparams.JUnitParamsRunner")
        || runnerName.startsWith("org.spockframework.runtime.Sputnik")
        || runnerName.startsWith("com.insightfullogic.lambdabehave")
        || runnerName.startsWith("com.googlecode.yatspec")
        || runnerName.startsWith("com.google.gwtmockito.GwtMockitoTestRunner");
  }

  private boolean isJUnitThreeSuiteMethodNotForOwnClass(final Runner runner,
      final String className) {
    // use strings in case this hack blows up due to internal junit change
    return runner.getClass().getName()
        .equals("org.junit.internal.runners.SuiteMethod")
        && !runner.getDescription().getClassName().equals(className);
  }

  private List<TestUnit> splitIntoFilteredUnits(final Description description) {
    return description.getChildren().stream()
        .filter(isTest())
        .map(descriptionToTestUnit())
        .collect(Collectors.toList());
  }

  private Function<Description, TestUnit> descriptionToTestUnit() {
    return a -> descriptionToTest(a);
  }

  private Predicate<Description> isTest() {
    return a -> a.isTest();
  }

  private TestUnit descriptionToTest(final Description description) {

    Class<?> clazz = description.getTestClass();
    if (clazz == null) {
      clazz = IsolationUtils.convertForClassLoader(
          IsolationUtils.getContextClassLoader(), description.getClassName());
    }
    return new AdaptedJUnitTestUnit(clazz,
        Optional.ofNullable(createFilterFor(description)));
  }

  private Filter createFilterFor(final Description description) {
    return new DescriptionFilter(description.toString());
  }

  @SuppressWarnings("rawtypes")
  private static Optional<Class> findClassRuleClass() {
    try {
      return Optional.<Class> ofNullable(Class.forName("org.junit.ClassRule"));
    } catch (final ClassNotFoundException ex) {
      return Optional.empty();
    }
  }

}
