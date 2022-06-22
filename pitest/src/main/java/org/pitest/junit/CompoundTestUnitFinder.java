package org.pitest.junit;

import java.util.Collections;
import java.util.List;

import org.pitest.testapi.TestUnit;
import org.pitest.testapi.TestUnitExecutionListener;
import org.pitest.testapi.TestUnitFinder;

public class CompoundTestUnitFinder implements TestUnitFinder {

  private final List<TestUnitFinder> tufs;

  public CompoundTestUnitFinder(final List<TestUnitFinder> tufs) {
    this.tufs = tufs;
  }

  @Override
  public List<TestUnit> findTestUnits(final Class<?> clazz, TestUnitExecutionListener listener) {
    for (final TestUnitFinder each : this.tufs) {
      final List<TestUnit> tus = each.findTestUnits(clazz, listener);
      if (!tus.isEmpty()) {
        return tus;
      }
    }
    return Collections.emptyList();
  }

}
