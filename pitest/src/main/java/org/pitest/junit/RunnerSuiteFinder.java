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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.internal.runners.SuiteMethod;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.junit.adapter.AdaptedJUnitTestUnit;
import org.pitest.testapi.TestSuiteFinder;

public class RunnerSuiteFinder implements TestSuiteFinder {

  @Override
  @SuppressWarnings("unchecked")
  public List<Class<?>> apply(final Class<?> a) {
    try {
      final Runner runner = AdaptedJUnitTestUnit.createRunner(a);

      final List<Description> allChildren = new ArrayList<Description>();
      flattenChildren(allChildren, runner.getDescription());

      final Set<Class<?>> classes = new LinkedHashSet<Class<?>>(
          runner.getDescription().getChildren().size());

      final List<Description> suites = FCollection.filter(allChildren,
          Prelude.or(isSuiteMethodRunner(runner), isSuite()));
      FCollection.flatMapTo(suites, descriptionToTestClass(), classes);

      classes.remove(a);
      return new ArrayList<Class<?>>(classes);
    } catch (RuntimeException ex) {
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
    return new Predicate<Description>() {
      @Override
      public Boolean apply(final Description a) {
        return SuiteMethod.class.isAssignableFrom(runner.getClass());
      }

    };
  }

  private static F<Description, Option<Class<?>>> descriptionToTestClass() {
    return new F<Description, Option<Class<?>>>() {

      @Override
      public Option<Class<?>> apply(final Description a) {
        final Class<?> clazz = a.getTestClass();
        if (clazz != null) {
          return Option.<Class<?>> some(clazz);
        } else {
          return Option.<Class<?>> none();
        }
      }

    };
  }

  private static Predicate<Description> isSuite() {
    return new Predicate<Description>() {
      @Override
      public Boolean apply(final Description a) {
        return a.isSuite();
      }

    };
  }

}
