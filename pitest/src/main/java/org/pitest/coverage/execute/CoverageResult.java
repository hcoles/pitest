package org.pitest.coverage.execute;

import java.util.Collection;

import org.pitest.Description;
import org.pitest.coverage.ClassStatistics;

public class CoverageResult {

  private final Description                 testUnitDescription;
  private final long                        executionTime;
  private final Collection<ClassStatistics> coverage;

  public CoverageResult(final Description testUnitDescription,
      final long executionTime, final Collection<ClassStatistics> coverage) {
    this.testUnitDescription = testUnitDescription;
    this.executionTime = executionTime;
    this.coverage = coverage;
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

  @Override
  public String toString() {
    return "CoverageResult [testUnitDescription=" + this.testUnitDescription
        + ", executionTime=" + this.executionTime + ", coverage="
        + this.coverage + "]";
  }

}
