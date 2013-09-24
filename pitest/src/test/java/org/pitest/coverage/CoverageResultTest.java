package org.pitest.coverage;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Test;
import org.pitest.coverage.ClassStatistics;
import org.pitest.coverage.CoverageResult;

public class CoverageResultTest {

  private CoverageResult testee;

  @Test
  public void shouldCalculateCorrectNumberOfCoveredLinesWhenNoneCovered() {
    this.testee = new CoverageResult(null, 0, true,
        Collections.<ClassStatistics> emptyList());
    assertEquals(0, this.testee.getNumberOfCoveredLines());
  }

  @Test
  public void shouldCalculateCorrectNumberOfCoveredLinesWhenOneClassHasCoverage() {
    this.testee = new CoverageResult(null, 0, true,
        Collections.singletonList(makeCoverage("foo", 42, 43)));
    assertEquals(2, this.testee.getNumberOfCoveredLines());
  }

  @Test
  public void shouldCalculateCorrectNumberOfCoveredLinesWhenMultiplesClassesHaveCoverage() {
    this.testee = new CoverageResult(null, 0, true, Arrays.asList(
        makeCoverage("foo", 42), makeCoverage("bar", 42, 43)));
    assertEquals(3, this.testee.getNumberOfCoveredLines());
  }

  private ClassStatistics makeCoverage(final String name, final int... lines) {
    final ClassStatistics cs = new ClassStatistics(name);
    for (final int i : lines) {
      cs.registerLineVisit(i);
    }
    return cs;
  }

}
