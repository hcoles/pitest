package org.pitest.testapi.execute;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.pitest.testapi.Configuration;
import org.pitest.testapi.NullExecutionListener;
import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitExecutionListener;

/**
 * Scans classes to discover TestUnits
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
    return StreamSupport.stream(classes.spliterator(), false)
            .flatMap(c -> Stream.concat(Stream.of(c), this.config.testSuiteFinder().apply(c).stream()))
            .distinct()
            .flatMap(c -> this.config.testUnitFinder().findTestUnits(c, listener).stream())
            .collect(Collectors.toList());
  }

}
