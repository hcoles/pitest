package org.pitest.extension.common;

import java.util.Collection;
import java.util.Collections;

import org.pitest.extension.Configuration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;
import org.pitest.internal.TestClass;

public class NoTestFinder implements TestUnitFinder {

  public Collection<TestUnit> findTestUnits(final TestClass clazz,
      final Configuration configuration, final TestDiscoveryListener listener,
      final TestUnitProcessor processor) {
    return Collections.emptyList();
  }

}
