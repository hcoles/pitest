package org.pitest.extension.common;

import java.util.Collection;
import java.util.Collections;

import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;

public class NoTestFinder implements TestUnitFinder {

  public Collection<TestUnit> findTestUnits(final Class<?> clazz,
      final TestDiscoveryListener listener) {
    return Collections.emptyList();
  }

}
