package org.pitest.mutationtest.report;

public class MutationTotals {

  private long numberOfClasses           = 0;
  private long numberOfLines             = 0;
  private long numberOfLinesCovered      = 0;
  private long numberOfMutations         = 0;
  private long numberOfMutationsDetected = 0;

  public long getNumberOfClasses() {
    return this.numberOfClasses;
  }

  public void addClasses(final long classes) {
    this.numberOfClasses += classes;
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
    return this.numberOfLines == 0 ? 100 : Math
        .round((100f * this.numberOfLinesCovered) / this.numberOfLines);
  }

  public int getMutationCoverage() {
    return this.numberOfMutations == 0 ? 100
        : Math.round((100f * this.numberOfMutationsDetected)
            / this.numberOfMutations);
  }

  public void add(final MutationTotals data) {
    this.addClasses(data.getNumberOfClasses());
    this.addLines(data.getNumberOfLines());
    this.addLinesCovered(data.getNumberOfLinesCovered());
    this.addMutations(data.getNumberOfMutations());
    this.addMutationsDetetcted(data.getNumberOfMutationsDetected());
  }

}