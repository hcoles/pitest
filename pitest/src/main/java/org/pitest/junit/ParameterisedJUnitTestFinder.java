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

import org.junit.internal.runners.ErrorReportingRunner;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runners.Parameterized;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.junit.adapter.AdaptedJUnitTestUnit;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitFinder;

public class ParameterisedJUnitTestFinder implements TestUnitFinder {
  @Override
  public List<TestUnit> findTestUnits(final Class<?> clazz) {

    final Runner runner = AdaptedJUnitTestUnit.createRunner(clazz);
    if ((runner == null)
        || runner.getClass().isAssignableFrom(ErrorReportingRunner.class)) {
      return Collections.emptyList();
    }

    if (isParameterizedTest(runner)) {
      return handleParameterizedTest(clazz, runner.getDescription());
    }

    return Collections.emptyList();

  }

  private List<TestUnit> handleParameterizedTest(final Class<?> clazz,
      final Description description) {
    final List<TestUnit> result = new ArrayList<TestUnit>();
    for (final Description each : description.getChildren()) {
      FCollection.mapTo(each.getChildren(), parameterizedToTestUnit(clazz),
          result);
    }
    return result;
  }

  private F<Description, TestUnit> parameterizedToTestUnit(final Class<?> clazz) {
    return new F<Description, TestUnit>() {

      @Override
      public TestUnit apply(final Description a) {
        return new AdaptedJUnitTestUnit(clazz,
            Option.<Filter> some(new ParameterisedTestFilter(a.toString())));
      }

    };
  }

  private boolean isParameterizedTest(final Runner runner) {
    return Parameterized.class.isAssignableFrom(runner.getClass());
  }

}
