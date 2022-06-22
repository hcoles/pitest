package org.pitest.testapi.execute;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.pitest.testapi.Configuration;
import org.pitest.testapi.NullExecutionListener;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitExecutionListener;

/**
 * Scans classes to discover TestUnits
 *
 */
public class FindTestUnits {

  private final Configuration config;
  private final TestUnitExecutionListener listener;

  public FindTestUnits(Configuration config) {
    this(config, new NullExecutionListener());
  }

  public FindTestUnits(Configuration config, TestUnitExecutionListener listener) {
    this.config = config;
    this.listener = listener;
  }

  public List<TestUnit> findTestUnitsForAllSuppliedClasses(Iterable<Class<?>> classes) {
    final List<TestUnit> testUnits = new ArrayList<>();

    for (final Class<?> c : classes) {
      final Collection<TestUnit> testUnitsFromClass = getTestUnits(c);
      testUnits.addAll(testUnitsFromClass);
    }

    return testUnits;

  }

  private Collection<TestUnit> getTestUnits(final Class<?> suiteClass) {
    final List<TestUnit> tus = new ArrayList<>();
    final Set<Class<?>> visitedClasses = new HashSet<>();
    findTestUnits(tus, visitedClasses, suiteClass);
    return tus;
  }

  private void findTestUnits(final List<TestUnit> tus,
      final Set<Class<?>> visitedClasses, final Class<?> suiteClass) {
    visitedClasses.add(suiteClass);
    final Collection<Class<?>> tcs = this.config.testSuiteFinder().apply(
        suiteClass);

    for (final Class<?> tc : tcs) {
      if (!visitedClasses.contains(tc)) {
        findTestUnits(tus, visitedClasses, tc);
      }
    }

    final List<TestUnit> testsInThisClass = this.config.testUnitFinder()
        .findTestUnits(suiteClass, listener);
    if (!testsInThisClass.isEmpty()) {
      tus.addAll(testsInThisClass);
    }

  }

}
