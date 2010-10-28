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
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.extension.Configuration;
import org.pitest.extension.Container;
import org.pitest.extension.GroupingStrategy;
import org.pitest.extension.ResultSource;
import org.pitest.extension.StaticConfigUpdater;
import org.pitest.extension.StaticConfiguration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.CompoundTestDiscoveryListener;
import org.pitest.functional.FCollection;
import org.pitest.internal.ContainerParser;
import org.pitest.internal.TestClass;

public class Pitest {

  private final static Logger       logger = Logger.getLogger(Pitest.class
                                               .getName());

  private final Configuration       initialConfig;
  private final StaticConfiguration initialStaticConfig;

  public Pitest(final StaticConfiguration initialStaticConfig,
      final Configuration initialConfig) {
    this.initialConfig = new ConcreteConfiguration(initialConfig);
    this.initialStaticConfig = initialStaticConfig;
  }

  public void run(final Container defaultContainer, final Class<?>... classes) {
    for (final Class<?> c : classes) {
      final Container container = new ContainerParser(c)
          .create(defaultContainer);
      StaticConfiguration staticConfig = new DefaultStaticConfig(
          this.initialStaticConfig);
      for (final StaticConfigUpdater each : this.initialConfig
          .staticConfigurationUpdaters()) {
        staticConfig = each.apply(staticConfig, c);
      }

      run(container, staticConfig, findTestUnitsForAllSuppliedClasses(
          this.initialConfig, new CompoundTestDiscoveryListener(staticConfig
              .getDiscoveryListeners()), staticConfig.getGroupingStrategy(), c));
    }
  }

  public void run(final Container container, final List<TestUnit> testUnits) {
    System.out.println("Running " + testUnits.size() + " tests");
    this.run(container, new DefaultStaticConfig(this.initialStaticConfig),
        testUnits);
  }

  private void run(final Container container,
      final StaticConfiguration staticConfig, final List<TestUnit> testUnits) {

    final Thread feederThread = startFeederThread(container, testUnits);

    processResultsFromQueue(container, feederThread, staticConfig);
  }

  public static List<TestUnit> findTestUnitsForAllSuppliedClasses(
      final Configuration startConfig, final TestDiscoveryListener listener,
      final GroupingStrategy groupStrategy, final Class<?>... classes) {
    final List<TestUnit> testUnits = new ArrayList<TestUnit>();

    for (final Class<?> c : classes) {
      final Collection<TestUnit> testUnitsFromClass = new TestClass(c)
          .getTestUnits(startConfig, listener);
      testUnits.addAll(groupStrategy.group(c, testUnitsFromClass));
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

    logger.info("Finished");

  }

  private Thread startFeederThread(final Container container,
      final List<TestUnit> callables) {
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
      FCollection.forEach(staticConfig.getTestListeners(), classifiedResult
          .getListenerFunction(result));
    }

  }

  // public void addListener(final TestListener listener) {
  // this.testListeners.add(listener);
  // }

}