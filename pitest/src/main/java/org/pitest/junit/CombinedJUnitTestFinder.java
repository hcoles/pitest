package org.pitest.junit;

import java.util.Arrays;
import java.util.Collection;

import org.pitest.extension.Configuration;
import org.pitest.extension.TestDiscoveryListener;
import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitFinder;
import org.pitest.extension.TestUnitProcessor;

public class CombinedJUnitTestFinder implements TestUnitFinder {

  private final TestUnitFinder        nonNativeFinder = new CompoundTestUnitFinder(
                                                          Arrays
                                                              .asList(
                                                                  new CustomJUnit3TestUnitFinder(),
                                                                  new JUnitCustomRunnerTestUnitFinder()));

  public Collection<TestUnit> findTestUnits(final Class<?> clazz,
      final Configuration configuration, final TestDiscoveryListener listener,
      final TestUnitProcessor processor) {
    // if (NativeJUnitTestFinder.canHandleNatively(clazz)) {
    // return this.nativeFinder.findTestUnits(clazz, configuration, listener,
    // processor);
    // } else {
    return this.nonNativeFinder.findTestUnits(clazz, configuration, listener,
        processor);
    // }
  }

}
