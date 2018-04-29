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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.junit.adapter.AdaptedJUnitTestUnit;
import org.pitest.testapi.TestSuiteFinder;

public class RunnerSuiteFinder implements TestSuiteFinder {

  @Override
  public List<Class<?>> apply(final Class<?> a) {
    try {
      final Runner runner = AdaptedJUnitTestUnit.createRunner(a);

      final List<Description> allChildren = new ArrayList<>();
      flattenChildren(allChildren, runner.getDescription());

      final List<Description> suites = FCollection.filter(allChildren,
          Prelude.or(isSuiteMethodRunner(runner), isSuite()));
      final Set<Class<?>> classes = suites.stream().flatMap(descriptionToTestClass()).collect(Collectors.toSet());

      classes.remove(a);
      return new ArrayList<>(classes);
    } catch (final RuntimeException ex) {
      // some runners (looking at you spock) can throw a runtime exception
      // when the getDescription method is called.
      return Collections.emptyList();
    }

  }

  private void flattenChildren(final List<Description> allChildren,
      final Description description) {
    for (final Description each : description.getChildren()) {
      allChildren.add(each);
      flattenChildren(allChildren, each);
    }
  }

  private static Predicate<Description> isSuiteMethodRunner(final Runner runner) {
    return a -> SuiteMethod.class.isAssignableFrom(runner.getClass());
  }

  private static Function<Description, Stream<Class<?>>> descriptionToTestClass() {
    return a -> {
      final Class<?> clazz = a.getTestClass();
      if (clazz != null) {
        return Stream.of(clazz);
      } else {
        return Stream.empty();
      }
    };
  }

  private static Predicate<Description> isSuite() {
    return a -> a.isSuite();
  }

}
