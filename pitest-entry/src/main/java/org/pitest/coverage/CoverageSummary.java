package org.pitest.coverage;

import static org.pitest.util.PercentageCalculator.getPercentage;

/**
 * Basic summary of line coverage data
 */
public final class CoverageSummary {

  private final int numberOfLines;
  private final int numberOfCoveredLines;

  public CoverageSummary(final int numberOfLines, final int numberOfCoveredLines) {
    this.numberOfLines = numberOfLines;
    this.numberOfCoveredLines = numberOfCoveredLines;
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
