package org.pitest.extension.common;

import java.util.Collections;
import java.util.List;

import org.pitest.testapi.TestClass;
import org.pitest.testapi.TestSuiteFinder;

public class NoTestSuiteFinder implements TestSuiteFinder {

  public List<TestClass> apply(final TestClass a) {
    return Collections.emptyList();
  }

}
