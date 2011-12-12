package org.pitest.junit;

import java.util.Collection;
import java.util.Collections;

import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;

public class CompoundTestUnitFinder implements TestUnitFinder {

  private final Collection<TestUnitFinder> tufs;

  public CompoundTestUnitFinder(final Collection<TestUnitFinder> tufs) {
    this.tufs = tufs;
  }

  public Collection<TestUnit> findTestUnits(final Class<?> clazz,
      final TestDiscoveryListener listener) {
    for (final TestUnitFinder each : this.tufs) {
      final Collection<TestUnit> tus = each.findTestUnits(clazz, listener);
      if (!tus.isEmpty()) {
        return tus;
      }
    }
    return Collections.emptyList();
  }

}
