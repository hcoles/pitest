package org.pitest.coverage;

import static org.pitest.util.PercentageCalculator.getPercentage;

/**
 * Basic summary of line coverage data
 */
public final class CoverageSummary {

  private final int numberOfTests;
  private final int numberOfLines;
  private final int numberOfCoveredLines;

  public CoverageSummary(int numberOfLines, int numberOfCoveredLines, int numberOfTests) {
    this.numberOfLines = numberOfLines;
    this.numberOfCoveredLines = numberOfCoveredLines;
    this.numberOfTests = numberOfTests;
  }

  public int getNumberOfTests() {
    return this.numberOfTests;
  }

  public int getNumberOfLines() {
    return this.numberOfLines;
  }

  public int getNumberOfCoveredLines() {
    return this.numberOfCoveredLines;
  }

  public int getCoverage() {
    return getPercentage(numberOfLines, numberOfCoveredLines);
  }

}
