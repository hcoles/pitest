package org.pitest.extension.common;

import java.util.Collections;
import java.util.List;

import org.pitest.extension.TestSuiteFinder;
import org.pitest.internal.TestClass;

public class NoTestSuiteFinder implements TestSuiteFinder {

  public List<TestClass> apply(final TestClass a) {
    return Collections.emptyList();
  }

}
