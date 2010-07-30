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
import org.pitest.extension.ResultSource;
import org.pitest.extension.StaticConfigUpdater;
import org.pitest.extension.StaticConfiguration;
import org.pitest.extension.TestUnit;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.ContainerParser;
import org.pitest.internal.TestClass;

public class Pitest {

  private final static Logger logger = Logger.getLogger(Pitest.class.getName());
  private final Configuration initialConfig;                                     ;

  public Pitest(final Configuration initialConfig) {
    this(new DefaultStaticConfig(), initialConfig);
  }

  public Pitest(final StaticConfiguration initialStaticConfig,
      final Configuration initialConfig) {
    this.initialConfig = new ConcreteConfiguration(initialConfig);
  }

  public void run(final Container defaultContainer, final Class<?>... classes) {
    this.run(defaultContainer, new DefaultStaticConfig(), classes);
  }

  public void run(final Container defaultContainer,
      final StaticConfiguration intialStaticConfig, final Class<?>... classes) {
    for (final Class<?> c : classes) {
      final Container container = new ContainerParser(c)
          .create(defaultContainer);
      StaticConfiguration staticConfig = new DefaultStaticConfig(
          intialStaticConfig);
      for (final StaticConfigUpdater each : this.initialConfig
          .staticConfigurationUpdaters()) {
        staticConfig = each.apply(staticConfig, c);
      }

      run(container, staticConfig, findTestUnitsForAllSuppliedClasses(
          this.initialConfig, c));
    }
  }

  public void run(final Container container, final List<TestUnit> testUnits) {
    this.run(container, new DefaultStaticConfig(), testUnits);
  }

  public void run(final Container container,
      final StaticConfiguration staticConfig, final List<TestUnit> testUnits) {

    final List<TestGroup> callables = processDependenciesIfRequired(container,
        testUnits);

    final Thread feederThread = startFeederThread(container, callables);

    processResultsFromQueue(container, feederThread, staticConfig);
  }

  private List<TestGroup> processDependenciesIfRequired(
      final Container container, final List<TestUnit> testUnits) {
    // for this optimisation to work finders must
    // return correctly ordered tests, they cannot rely
    // on dependencies to enforce order, only grouping
    if (container.canParallise()) {
      return createGroups(testUnits);
    } else {
      final List<TestGroup> callables = new ArrayList<TestGroup>(1);
      callables.add(new TestGroup(testUnits));
      return callables;
    }
  }

  public static List<TestUnit> findTestUnitsForAllSuppliedClasses(
      final Configuration startConfig, final Class<?>... classes) {
    final List<TestUnit> testUnits = new ArrayList<TestUnit>();

    for (final Class<?> c : classes) {
      testUnits.addAll(new TestClass(c).getTestUnits(startConfig));
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

  private List<TestGroup> createGroups(

  final Collection<TestUnit> testUnits) {

    final List<TestGroup> groupedTests = new ArrayList<TestGroup>();
    createGroups(testUnits, groupedTests);

    logger.info("Tests will run as " + groupedTests.size() + " groups");

    return groupedTests;

  }

  private void createGroups(final Collection<TestUnit> tus,
      final List<TestGroup> groups) {
    final List<TestUnit> remainder = new ArrayList<TestUnit>();
    for (final TestUnit tu : tus) {
      if (tu.dependsOn().hasNone()) {
        final TestGroup l = new TestGroup();
        l.add(tu);
        groups.add(l);
      } else {
        for (final TestUnit each : addToGroupIfPossible(tu, groups)) {
          remainder.add(each);
        }

      }
    }

    if (!remainder.isEmpty()) {
      createGroups(remainder, groups);
    }
  }

  private Option<TestUnit> addToGroupIfPossible(final TestUnit tu,
      final List<TestGroup> groups) {
    for (final TestGroup group : groups) {
      if (group.contains(tu.dependsOn().value())) {
        group.add(tu);
        return Option.none();
      }
    }
    return Option.someOrNone(tu);
  }

  private Thread startFeederThread(final Container container,
      final List<TestGroup> callables) {
    final Runnable feeder = new Runnable() {
      public void run() {
        for (final TestGroup group : callables) {
          container.submit(group);
        }
        callables.clear();
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
