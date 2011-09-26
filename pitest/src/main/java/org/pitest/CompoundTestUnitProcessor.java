package org.pitest;

import java.util.Collection;
import java.util.List;

import org.pitest.extension.TestUnit;
import org.pitest.extension.TestUnitProcessor;

public class CompoundTestUnitProcessor implements TestUnitProcessor {

  private final Collection<TestUnitProcessor> children;

  public CompoundTestUnitProcessor(final List<TestUnitProcessor> tups) {
    this.children = tups;
  }

  public TestUnit apply(final TestUnit tu) {
    TestUnit alteredTestUnit = tu;
    for (final TestUnitProcessor tup : this.children) {
      alteredTestUnit = tup.apply(alteredTestUnit);
    }

    return alteredTestUnit;
  }

}
