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

package org.pitest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.extension.Configuration;
import org.pitest.extension.Container;
import org.pitest.extension.GroupingStrategy;
import org.pitest.extension.ResultSource;
import org.pitest.extension.StaticConfiguration;
import org.pitest.extension.TestListener;
import org.pitest.extension.TestUnit;
import org.pitest.functional.FCollection;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.TestClass;
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

  public void run(final Container container, final Configuration config,
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

    LOG.info("Running " + testUnits.size() + " units");

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
      final Collection<TestUnit> testUnitsFromClass = new TestClass(c)
          .getTestUnits(startConfig, groupStrategy);
      testUnits.addAll(testUnitsFromClass);
    }

    return testUnits;

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

    LOG.info("Finished");

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
    feederThread.setDaemon(true);
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