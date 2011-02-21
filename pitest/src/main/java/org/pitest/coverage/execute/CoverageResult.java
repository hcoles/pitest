package org.pitest.coverage.execute;

import java.util.Collection;

import org.pitest.Description;
import org.pitest.coverage.ClassStatistics;

public class CoverageResult {

  private final Description                 testUnitDescription;
  private final long                        executionTime;
  private final Collection<ClassStatistics> coverage;
  private final boolean                     greenSuite;

  public CoverageResult(final Description testUnitDescription,
      final long executionTime, final boolean greenSuite,
      final Collection<ClassStatistics> coverage) {
    this.testUnitDescription = testUnitDescription;
    this.executionTime = executionTime;
    this.coverage = coverage;
    this.greenSuite = greenSuite;
  }

  public Description getTestUnitDescription() {
    return this.testUnitDescription;
  }

  public long getExecutionTime() {
    return this.executionTime;
  }

  public Collection<ClassStatistics> getCoverage() {
    return this.coverage;
  }

  public boolean isGreenTest() {
    return this.greenSuite;
  }

  @Override
  public String toString() {
    return "CoverageResult [testUnitDescription=" + this.testUnitDescription
        + ", executionTime=" + this.executionTime + ", coverage="
        + this.coverage + ", greenSuite=" + this.greenSuite + "]";
  }

}
