package org.pitest;

import java.util.Collection;
import java.util.Collections;

import org.pitest.extension.TestSuiteFinder;
import org.pitest.internal.TestClass;

public class CompoundTestSuiteFinder implements TestSuiteFinder {

  private final Collection<TestSuiteFinder> children;

  public CompoundTestSuiteFinder(final Collection<TestSuiteFinder> children) {
    this.children = children;
  }

  public Collection<TestClass> apply(final TestClass a) {
    for (final TestSuiteFinder i : this.children) {
      final Collection<TestClass> found = i.apply(a);
      if (!found.isEmpty()) {
        return found;
      }

    }

    return Collections.emptyList();
  }

}
