package org.pitest.coverage.execute;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.pitest.dependency.DependencyExtractor;
import org.pitest.functional.FCollection;
import org.pitest.testapi.TestUnit;
import org.pitest.util.Unchecked;

class DependencyFilter {

  private final DependencyExtractor analyser;
  private final Predicate<String>   filter;

  DependencyFilter(final DependencyExtractor analyser,
      final Predicate<String> filter) {
    this.analyser = analyser;
    this.filter = filter;
  }

  List<TestUnit> filterTestsByDependencyAnalysis(final List<TestUnit> tus) {
    if (this.analyser.getMaxDistance() < 0) {
      return tus;
    } else {
      return FCollection.filter(tus, isWithinReach());
    }
  }

  private Predicate<TestUnit> isWithinReach() {

    return new Predicate<TestUnit>() {
      private final Map<String, Boolean> cache = new HashMap<>();

      @Override
      public boolean test(final TestUnit testUnit) {
        final String testClass = testUnit.getDescription().getFirstTestClass();
        try {
          if (this.cache.containsKey(testClass)) {
            return this.cache.get(testClass);
          } else {
            final boolean inReach = !DependencyFilter.this.analyser
                .extractCallDependenciesForPackages(testClass,
                    DependencyFilter.this.filter).isEmpty();
            this.cache.put(testClass, inReach);
            return inReach;
          }

        } catch (final IOException e) {
          throw Unchecked.translateCheckedException(e);
        }
      }

    };
  }

}
