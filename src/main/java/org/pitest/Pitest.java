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

import org.pitest.extension.Configuration;
import org.pitest.extension.Container;
import org.pitest.extension.ResultSource;
import org.pitest.extension.TestListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.common.ConsoleResultListener;
import org.pitest.functional.Common;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.internal.ConfigParser;
import org.pitest.internal.ContainerParser;
import org.pitest.internal.TestClass;

public class Pitest {

  // things that cannot be overridden by child suites
  private final List<TestListener> resultListeners = new ArrayList<TestListener>();
  private final ResultClassifier   classifier      = new ResultClassifier();
  // test filters

  private final Container          defaultContainer;
  private final Configuration      initialConfig;

  public Pitest(final Container container, final Configuration initialConfig) {
    this.resultListeners.add(ConsoleResultListener.instance());
    this.defaultContainer = container;
    this.initialConfig = initialConfig;
  }

  public void run(final Class<?>... classes) {
    for (final Class<?> c : classes) {
      final Container container = new ContainerParser(c)
          .create(this.defaultContainer);
      run(container, findTestUnitsForAllSuppliedClasses(this.initialConfig, c));
    }
  }

  public void run(final Container container, final List<TestUnit> testUnits) {
    FCollection.forEach(testUnits, Common.print());

    final Thread feederThread = startFeederThread(container,
        createGroups(testUnits));

    processResultsFromQueue(container, feederThread);
  }

  public static List<TestUnit> findTestUnitsForAllSuppliedClasses(
      final Configuration startConfig, final Class<?>... classes) {
    final List<TestUnit> testUnits = new ArrayList<TestUnit>();

    for (final Class<?> c : classes) {
      final Configuration classConfig = new ConfigParser(c).create(startConfig);
      testUnits.addAll(new TestClass(c).getTestUnits(classConfig));
    }
    return testUnits;
  }

  private void processResultsFromQueue(final Container container,
      final Thread feederThread) {

    final ResultSource results = container.getResultSource();

    while (feederThread.isAlive()) {
      try {
        feederThread.join(20);
      } catch (final InterruptedException e) {
        // swallow
      }
      processResults(results);
    }

    container.shutdownWhenProcessingComplete();

    while (!container.awaitCompletion() || results.resultsAvailable()) {
      processResults(results);
    }

  }

  private List<TestGroup> createGroups(

  final Collection<TestUnit> testUnits) {

    final List<TestGroup> groupedTests = new ArrayList<TestGroup>();
    createGroups(testUnits, groupedTests);

    System.out.println("Tests will run as " + groupedTests.size() + " groups");

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

  private void processResults(final ResultSource source) {
    final List<TestResult> results = source.getAvailableResults();
    for (final TestResult result : results) {
      final ResultType classifiedResult = this.classifier.apply(result);
      FCollection.forEach(this.resultListeners, classifiedResult
          .getListenerFunction(result));
    }

  }

  public void addListener(final TestListener l) {
    this.resultListeners.add(l);
  }

}
