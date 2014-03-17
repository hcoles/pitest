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

package org.pitest.execute;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.testapi.Configuration;
import org.pitest.testapi.GroupingStrategy;
import org.pitest.testapi.TestListener;
import org.pitest.testapi.TestResult;
import org.pitest.testapi.TestUnit;
import org.pitest.util.Log;

public class Pitest {

  private final static Logger       LOG = Log.getLogger();

  private final StaticConfiguration initialStaticConfig;

  public Pitest(final StaticConfiguration initialStaticConfig) {
    this.initialStaticConfig = initialStaticConfig;
  }

  public void run(final Container defaultContainer, final Configuration config,
      final Class<?>... classes) {
    run(defaultContainer, config, Arrays.asList(classes));
  }

  private void run(final Container container, final Configuration config,
      final Collection<Class<?>> classes) {

    run(container,
        this.initialStaticConfig,
        findTestUnitsForAllSuppliedClasses(config,
            this.initialStaticConfig.getGroupingStrategy(), classes));

  }

  // entry point for mutation testing
  public void run(final Container container,
      final List<? extends TestUnit> testUnits) {
    this.run(container, this.initialStaticConfig, testUnits);
  }

  private void run(final Container container,
      final StaticConfiguration staticConfig,
      final List<? extends TestUnit> testUnits) {

    LOG.fine("Running " + testUnits.size() + " units");

    signalRunStartToAllListeners(staticConfig);

    final Thread feederThread = startFeederThread(container, testUnits);

    processResultsFromQueue(container, feederThread, staticConfig);
  }

  private void signalRunStartToAllListeners(
      final StaticConfiguration staticConfig) {
    FCollection.forEach(staticConfig.getTestListeners(),
        new SideEffect1<TestListener>() {
          public void apply(final TestListener a) {
            a.onRunStart();
          }
        });
  }

  public static List<TestUnit> findTestUnitsForAllSuppliedClasses(
      final Configuration startConfig, final GroupingStrategy groupStrategy,
      final Iterable<Class<?>> classes) {
    final List<TestUnit> testUnits = new ArrayList<TestUnit>();

    for (final Class<?> c : classes) {
      final Collection<TestUnit> testUnitsFromClass = getTestUnits(c,
          startConfig, groupStrategy);
      testUnits.addAll(testUnitsFromClass);
    }

    return testUnits;

  }

  private static Collection<TestUnit> getTestUnits(final Class<?> suiteClass,
      final Configuration startConfig, final GroupingStrategy groupStrategy) {

    final List<TestUnit> tus = new ArrayList<TestUnit>();
    final Set<Class<?>> visitedClasses = new HashSet<Class<?>>();
    findTestUnits(tus, visitedClasses, suiteClass, startConfig, groupStrategy);
    return tus;
  }

  private static void findTestUnits(final List<TestUnit> tus,
      final Set<Class<?>> visitedClasses, final Class<?> suiteClass,
      final Configuration startConfig, final GroupingStrategy groupStrategy) {
    visitedClasses.add(suiteClass);
    final Collection<Class<?>> tcs = startConfig.testSuiteFinder().apply(
        suiteClass);

    for (final Class<?> tc : tcs) {
      if (!visitedClasses.contains(tc)) {
        findTestUnits(tus, visitedClasses, tc, startConfig, groupStrategy);
      }
    }

    final List<TestUnit> testsInThisClass = getTestUnitsWithinClass(suiteClass,
        startConfig);
    if (!testsInThisClass.isEmpty()) {
      tus.addAll(groupStrategy.group(suiteClass, testsInThisClass));
    }

  }

  private static List<TestUnit> getTestUnitsWithinClass(
      final Class<?> suiteClass, final Configuration classConfig) {
    return classConfig.testUnitFinder().findTestUnits(suiteClass);
  }

  private void processResultsFromQueue(final Container container,
      final Thread feederThread, final StaticConfiguration staticConfig) {

    final ResultSource results = container.getResultSource();

    boolean isAlive = feederThread.isAlive();
    while (isAlive) {
      processResults(staticConfig, results);
      try {
        feederThread.join(100);
      } catch (final InterruptedException e) {
        // swallow
      }
      isAlive = feederThread.isAlive();

    }

    container.shutdownWhenProcessingComplete();

    while (!container.awaitCompletion() || results.resultsAvailable()) {
      processResults(staticConfig, results);
    }

    signalRunEndToAllListeners(staticConfig);

    LOG.fine("Finished");

  }

  private void signalRunEndToAllListeners(final StaticConfiguration staticConfig) {
    FCollection.forEach(staticConfig.getTestListeners(),
        new SideEffect1<TestListener>() {
          public void apply(final TestListener a) {
            a.onRunEnd();
          }
        });
  }

  private Thread startFeederThread(final Container container,
      final List<? extends TestUnit> callables) {
    final Runnable feeder = new Runnable() {
      public void run() {
        for (final TestUnit unit : callables) {
          container.submit(unit);
        }
      }
    };
    final Thread feederThread = new Thread(feeder);
    feederThread.start();
    return feederThread;
  }

  private void processResults(final StaticConfiguration staticConfig,
      final ResultSource source) {
    final List<TestResult> results = source.getAvailableResults();

    for (final TestResult result : results) {
      final ResultType classifiedResult = staticConfig.getClassifier()
          .classify(result);
      FCollection.forEach(staticConfig.getTestListeners(),
          classifiedResult.getListenerFunction(result));
    }

  }

}