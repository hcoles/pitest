package org.pitest.coverage.execute;

import java.io.Serializable;
import java.util.Collection;

import org.pitest.Description;
import org.pitest.coverage.ClassStatistics;

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

  @Override
  public String toString() {
    return "CoverageResult [testUnitDescription=" + this.testUnitDescription
        + ", executionTime=" + this.executionTime + ", coverage="
        + this.coverage + ", greenSuite=" + this.greenSuite + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result
        + ((this.coverage == null) ? 0 : this.coverage.hashCode());
    result = prime * result + this.executionTime;
    result = prime * result + (this.greenSuite ? 1231 : 1237);
    result = prime
        * result
        + ((this.testUnitDescription == null) ? 0 : this.testUnitDescription
            .hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final CoverageResult other = (CoverageResult) obj;
    if (this.coverage == null) {
      if (other.coverage != null) {
        return false;
      }
    } else if (!this.coverage.equals(other.coverage)) {
      return false;
    }
    if (this.executionTime != other.executionTime) {
      return false;
    }
    if (this.greenSuite != other.greenSuite) {
      return false;
    }
    if (this.testUnitDescription == null) {
      if (other.testUnitDescription != null) {
        return false;
      }
    } else if (!this.testUnitDescription.equals(other.testUnitDescription)) {
      return false;
    }
    return true;
  }

}
