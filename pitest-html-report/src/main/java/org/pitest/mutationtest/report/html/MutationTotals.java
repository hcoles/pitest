package org.pitest.mutationtest.report.html;

import static org.pitest.util.PercentageCalculator.getPercentage;

import java.math.BigDecimal;

public class MutationTotals {

  private long numberOfFiles                 = 0;
  private long numberOfLines                 = 0;
  private long numberOfLinesCovered          = 0;
  private long numberOfMutations             = 0;
  private long numberOfMutationsDetected     = 0;
  private long numberOfMutationsWithCoverage = 0;
  private int thresholdPrecision = 0;

  public long getNumberOfFiles() {
    return this.numberOfFiles;
  }

  public void addFiles(final long files) {
    this.numberOfFiles += files;
  }

  public long getNumberOfLines() {
    return this.numberOfLines;
  }

  public void addLines(final long lines) {
    this.numberOfLines += lines;
  }

  public long getNumberOfLinesCovered() {
    return this.numberOfLinesCovered;
  }

  public void addLinesCovered(final long linesCovered) {
    this.numberOfLinesCovered += linesCovered;
  }

  public long getNumberOfMutations() {
    return this.numberOfMutations;
  }

  public void addMutations(final long mutations) {
    this.numberOfMutations += mutations;
  }

  public long getNumberOfMutationsDetected() {
    return this.numberOfMutationsDetected;
  }

  public void addMutationsDetetcted(final long mutationsKilled) {
    this.numberOfMutationsDetected += mutationsKilled;
  }

  public int getLineCoverage() {
    return getPercentage(numberOfLines, numberOfLinesCovered);
  }

  public int getMutationCoverage() {
    return getPercentage(numberOfMutations, numberOfMutationsDetected);
  }

  public void addMutationsWithCoverage(final long mutationsWithCoverage) {
    this.numberOfMutationsWithCoverage += mutationsWithCoverage;
  }

  public int getTestStrength() {
    return getPercentage(numberOfMutationsWithCoverage, numberOfMutationsDetected);
  }

  public long getNumberOfMutationsWithCoverage() {
    return this.numberOfMutationsWithCoverage;
  }

  public void setThresholdPrecision(int thresholdPrecision) {
    this.thresholdPrecision = thresholdPrecision;
  }

  public BigDecimal getLineCoverage(int precision) {
    return getPercentage(numberOfLines, numberOfLinesCovered, precision);
  }

  public BigDecimal getMutationCoverage(int precision) {
    return getPercentage(numberOfMutations, numberOfMutationsDetected, precision);
  }

  public BigDecimal getTestStrength(int precision) {
    return getPercentage(numberOfMutationsWithCoverage, numberOfMutationsDetected, precision);
  }

  public String getLineCoverageLabel() {
    if (thresholdPrecision == 0) {
      return String.valueOf(getLineCoverage());
    }
    return getLineCoverage(thresholdPrecision).toPlainString();
  }

  public String getMutationCoverageLabel() {
    if (thresholdPrecision == 0) {
      return String.valueOf(getMutationCoverage());
    }
    return getMutationCoverage(thresholdPrecision).toPlainString();
  }

  public String getTestStrengthLabel() {
    if (thresholdPrecision == 0) {
      return String.valueOf(getTestStrength());
    }
    return getTestStrength(thresholdPrecision).toPlainString();
  }

  public void add(final MutationTotals data) {
    add(data.getNumberOfLines(), data.getNumberOfFiles(), data);
  }

  private void add(final long lines, final long files, final MutationTotals data) {
    this.addFiles(files);
    this.addLines(lines);
    this.addLinesCovered(data.getNumberOfLinesCovered());
    this.addMutations(data.getNumberOfMutations());
    this.addMutationsDetetcted(data.getNumberOfMutationsDetected());
    this.addMutationsWithCoverage(data.getNumberOfMutationsWithCoverage());
  }
}
