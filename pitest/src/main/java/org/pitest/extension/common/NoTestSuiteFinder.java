package org.pitest.extension.common;

import java.util.Collection;
import java.util.Collections;

import org.pitest.extension.TestSuiteFinder;
import org.pitest.internal.TestClass;

public class NoTestSuiteFinder implements TestSuiteFinder {

  public Collection<TestClass> apply(final TestClass a) {
    return Collections.emptyList();
  }

}
