package org.pitest.coverage;

/**
 * Basic summary of line coverage data
 */
public final class CoverageSummary {

  private final int numberOfLines;
  private final int numberOfCoveredLines;

  public CoverageSummary(int numberOfLines, int numberOfCoveredLines) {
    this.numberOfLines = numberOfLines;
    this.numberOfCoveredLines = numberOfCoveredLines;
  }

  public int getNumberOfLines() {
    return numberOfLines;
  }

  public int getNumberOfCoveredLines() {
    return numberOfCoveredLines;
  }

  public int getCoverage() {
    return numberOfLines == 0 ? 100 : Math.round((100f * numberOfCoveredLines) / numberOfLines);
  }
}
