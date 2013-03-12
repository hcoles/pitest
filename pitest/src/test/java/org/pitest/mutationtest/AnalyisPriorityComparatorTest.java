package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.Description;
import org.pitest.extension.ResultCollector;

public class AnalyisPriorityComparatorTest {
  
  private AnalyisPriorityComparator testee = new AnalyisPriorityComparator();

  @Test
  public void shouldPrioritiseLargestFirst() {
    MutationAnalysisUnit a = unit(1);
    MutationAnalysisUnit b = unit(2);
    MutationAnalysisUnit c = unit(3);
    List<MutationAnalysisUnit> actual = Arrays.asList(a,b,c);
    Collections.sort(actual, testee);
    assertEquals(Arrays.asList(c,b,a), actual);
  }

  @Test
  public void shouldPreserveCorrectOrder() {
    MutationAnalysisUnit a = unit(3);
    MutationAnalysisUnit b = unit(2);
    MutationAnalysisUnit c = unit(1);
    List<MutationAnalysisUnit> actual = Arrays.asList(a,b,c);
    Collections.sort(actual, testee);
    assertEquals(Arrays.asList(a,b,c), actual);
  }
  

  private MutationAnalysisUnit unit(final int count) {
    return new MutationAnalysisUnit() {

      public void execute(ClassLoader loader, ResultCollector rc) {

      }

      public Description getDescription() {
        return null;
      }

      public int priority() {
        return count;
      }
      
      @Override
      public String toString() {
        return ""+count;
      }
      
    };
  }

}
