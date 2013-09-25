package org.pitest.extension.common;

import java.util.Collections;
import java.util.List;

import org.pitest.testapi.TestSuiteFinder;

public class NoTestSuiteFinder implements TestSuiteFinder {

  public List<Class<?>> apply(final Class<?> a) {
    return Collections.emptyList();
  }

}
