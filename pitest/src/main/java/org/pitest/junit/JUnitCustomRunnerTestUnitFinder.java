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

import java.util.Collection;
import java.util.Collections;

import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.pitest.extension.Configuration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.IsolationUtils;
import org.pitest.junit.adapter.AdaptedJUnitTestUnit;

public class JUnitCustomRunnerTestUnitFinder implements TestUnitFinder {

  public Collection<TestUnit> findTestUnits(final Class<?> testClass,
      final Configuration config, final TestDiscoveryListener listener,
      final TestUnitProcessor processor) {

    final Collection<? extends TestUnit> units = createUnits(testClass,
        listener);

    return FCollection.map(units, processor);

  }

  private Collection<? extends TestUnit> createUnits(final Class<?> clazz,
      final TestDiscoveryListener listener) {
    final Runner runner = AdaptedJUnitTestUnit.createRunner(clazz);
    if (Filterable.class.isAssignableFrom(runner.getClass())) {
      return splitIntoFilteredUnits(runner.getDescription(), listener);
    } else {
      return Collections.<TestUnit> singletonList(new AdaptedJUnitTestUnit(
          clazz, Option.<Filter> none()));
    }
  }

  private Collection<? extends TestUnit> splitIntoFilteredUnits(
      final Description description, final TestDiscoveryListener listener) {

    listener.enterClass(description.getTestClass());
    final Collection<TestUnit> tus = FCollection.filter(
        description.getChildren(), isTest()).map(descriptionToTestUnit());
    listener.receiveTests(tus);
    listener.leaveClass(description.getTestClass());

    return tus;
  }

  private F<Description, TestUnit> descriptionToTestUnit() {
    return new F<Description, TestUnit>() {

      public TestUnit apply(final Description a) {
        return descriptionToTest(a);
      }

    };
  }

  private F<Description, Boolean> isTest() {
    return new F<Description, Boolean>() {

      public Boolean apply(final Description a) {
        return a.isTest();
      }

    };
  }

  private TestUnit descriptionToTest(final Description description) {

    Class<?> clazz = description.getTestClass();
    if (clazz == null) {
      clazz = IsolationUtils.convertForClassLoader(
          IsolationUtils.getContextClassLoader(), description.getClassName());
    }
    return new AdaptedJUnitTestUnit(clazz,
        Option.some(createFilterFor(description)));
  }

  private Filter createFilterFor(final Description description) {
    return new DescriptionFilter(description.toString());
  }

}
