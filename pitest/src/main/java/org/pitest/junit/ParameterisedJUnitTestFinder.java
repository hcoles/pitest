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
package org.pitest.junit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import org.junit.experimental.categories.Category;
import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.pitest.functional.FCollection;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.pitest.junit.adapter.AdaptedJUnitTestUnit;
import org.pitest.testapi.TestGroupConfig;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitExecutionListener;
import org.pitest.testapi.TestUnitFinder;

public class ParameterisedJUnitTestFinder implements TestUnitFinder {
  private final TestGroupConfig config;

    public ParameterisedJUnitTestFinder(TestGroupConfig config) {
        this.config = config;
    }

    @Override
  public List<TestUnit> findTestUnits(final Class<?> clazz, TestUnitExecutionListener unused) {

    final Runner runner = AdaptedJUnitTestUnit.createRunner(clazz);
    if ((runner == null)
        || runner.getClass().isAssignableFrom(ErrorReportingRunner.class)) {
      return Collections.emptyList();
    }

    if (isParameterizedTest(runner) && isIncluded(clazz)) {
      return handleParameterizedTest(clazz, runner.getDescription());
    }

    return Collections.emptyList();

  }

  private List<TestUnit> handleParameterizedTest(final Class<?> clazz,
      final Description description) {
    final List<TestUnit> result = new ArrayList<>();
    for (final Description each : description.getChildren()) {
      FCollection.mapTo(each.getChildren(), parameterizedToTestUnit(clazz),
          result);
    }
    return result;
  }

  private Function<Description, TestUnit> parameterizedToTestUnit(final Class<?> clazz) {
    return a -> new AdaptedJUnitTestUnit(clazz,
        Optional.ofNullable(new ParameterisedTestFilter(a.toString())));
  }

  private boolean isParameterizedTest(final Runner runner) {
    return Parameterized.class.isAssignableFrom(runner.getClass());
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
    return Stream.of(c)
            .flatMap(toCategoryNames())
            .collect(Collectors.toList());
  }

  private Function<Category, Stream<String>> toCategoryNames() {
    return a -> {
      if (a == null) {
        return Stream.empty();
      }
      return Stream.of(a.value())
              .map(Class::getName);
    };
  }

}
