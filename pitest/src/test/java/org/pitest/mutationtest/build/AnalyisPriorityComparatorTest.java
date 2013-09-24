package org.pitest.mutationtest.build;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.mutationtest.build.AnalysisPriorityComparator;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;

public class AnalyisPriorityComparatorTest {

  private final AnalysisPriorityComparator testee = new AnalysisPriorityComparator();

  @Test
  public void shouldPrioritiseLargestFirst() {
    final MutationAnalysisUnit a = unit(1);
    final MutationAnalysisUnit b = unit(2);
    final MutationAnalysisUnit c = unit(3);
    final List<MutationAnalysisUnit> actual = Arrays.asList(a, b, c);
    Collections.sort(actual, this.testee);
    assertEquals(Arrays.asList(c, b, a), actual);
  }

  @Test
  public void shouldPreserveCorrectOrder() {
    final MutationAnalysisUnit a = unit(3);
    final MutationAnalysisUnit b = unit(2);
    final MutationAnalysisUnit c = unit(1);
    final List<MutationAnalysisUnit> actual = Arrays.asList(a, b, c);
    Collections.sort(actual, this.testee);
    assertEquals(Arrays.asList(a, b, c), actual);
  }

  private MutationAnalysisUnit unit(final int count) {
    return new MutationAnalysisUnit() {

      public void execute(final ClassLoader loader, final ResultCollector rc) {

      }

      public Description getDescription() {
        return null;
      }

      public int priority() {
        return count;
      }

      @Override
      public String toString() {
        return "" + count;
      }

    };
  }

}
