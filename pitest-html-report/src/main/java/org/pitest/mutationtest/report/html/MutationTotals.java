package org.pitest.mutationtest.report.html;

import static org.pitest.util.PercentageCalculator.getPercentage;

public class MutationTotals {

  private long numberOfFiles                 = 0;
  private long numberOfLines                 = 0;
  private long numberOfLinesCovered          = 0;
  private long numberOfMutations             = 0;
  private long numberOfMutationsDetected     = 0;
  private long numberOfMutationsWithCoverage = 0;

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
