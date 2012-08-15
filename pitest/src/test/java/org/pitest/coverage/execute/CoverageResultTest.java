package org.pitest.coverage.execute;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.pitest.coverage.ClassStatistics;


public class CoverageResultTest {
  
  private CoverageResult testee;
  
  @Test
  public void shouldCalculateCorrectNumberOfCoveredLinesWhenNoneCovered() {
    testee = new CoverageResult(null,0, true, Collections.<ClassStatistics>emptyList());
    assertEquals(0, testee.getNumberOfCoveredLines());
  }
  
  @Test
  public void shouldCalculateCorrectNumberOfCoveredLinesWhenOneClassHasCoverage() {
    testee = new CoverageResult(null,0, true, Collections.singletonList(makeCoverage("foo",42,43)));
    assertEquals(2, testee.getNumberOfCoveredLines());
  }
  
  @Test
  public void shouldCalculateCorrectNumberOfCoveredLinesWhenMultiplesClassesHaveCoverage() {
    testee = new CoverageResult(null,0, true, Arrays.asList(makeCoverage("foo",42), makeCoverage("bar",42,43)));
    assertEquals(3, testee.getNumberOfCoveredLines());
  }

  private ClassStatistics makeCoverage(String name, int ... lines) {
    ClassStatistics cs = new ClassStatistics(name);
    for ( int i : lines) {
      cs.registerLineVisit(i);
    }
    return cs;
  }

}
