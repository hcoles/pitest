package org.pitest.coverage;

import java.io.Serializable;
import java.util.Collection;

import org.pitest.functional.F2;
import org.pitest.functional.FCollection;
import org.pitest.testapi.Description;

public class CoverageResult implements Serializable {

  private static final long                 serialVersionUID = 1L;

  private final Description                 testUnitDescription;
  private final int                         executionTime;
  private final Collection<ClassStatistics> coverage;
  private final boolean                     greenSuite;

  public CoverageResult(final Description testUnitDescription,
      final int executionTime, final boolean greenSuite,
      final Collection<ClassStatistics> coverage) {
    this.testUnitDescription = testUnitDescription;
    this.executionTime = executionTime;
    this.coverage = coverage;
    this.greenSuite = greenSuite;
  }

  public Description getTestUnitDescription() {
    return this.testUnitDescription;
  }

  public int getExecutionTime() {
    return this.executionTime;
  }

  public Collection<ClassStatistics> getCoverage() {
    return this.coverage;
  }

  public boolean isGreenTest() {
    return this.greenSuite;
  }

  public int getNumberOfCoveredLines() {
    return FCollection.fold(classStatisticsToLineCount(), 0, this.coverage);
  }

  private static F2<Integer, ClassStatistics, Integer> classStatisticsToLineCount() {
    return new F2<Integer, ClassStatistics, Integer>() {
      public Integer apply(final Integer a, final ClassStatistics b) {
        return a + b.getUniqueVisitedLines().size();
      }

    };
  }

  @Override
  public String toString() {
    return "CoverageResult [testUnitDescription=" + this.testUnitDescription
        + ", executionTime=" + this.executionTime + ", coverage="
        + this.coverage + ", greenSuite=" + this.greenSuite + "]";
  }

}
