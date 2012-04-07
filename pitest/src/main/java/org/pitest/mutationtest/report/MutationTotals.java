package org.pitest.mutationtest.report;

public class MutationTotals {
  
  private long numberOfClasses           = 0;
  private long numberOfLines             = 0;
  private long numberOfLinesCovered      = 0;
  private long numberOfMutations         = 0;
  private long numberOfMutationsDetected = 0;

  public long getNumberOfClasses() {
    return numberOfClasses;
  }

  public void addClasses(long classes) {
    this.numberOfClasses += classes;
  }

  public long getNumberOfLines() {
    return numberOfLines;
  }

  public void addLines(long lines) {
    this.numberOfLines += lines;
  }

  public long getNumberOfLinesCovered() {
    return numberOfLinesCovered;
  }

  public void addLinesCovered(long linesCovered) {
    this.numberOfLinesCovered += linesCovered;
  }

  public long getNumberOfMutations() {
    return numberOfMutations;
  }

  public void addMutations(long mutations) {
    this.numberOfMutations += mutations;
  }

  public long getNumberOfMutationsDetected() {
    return numberOfMutationsDetected;
  }

  public void addMutationsDetetcted(long mutationsKilled) {
    this.numberOfMutationsDetected += mutationsKilled;
  }

  public int getLineCoverage() {
    return numberOfLines == 0 ? 100 : Math.round((100f * numberOfLinesCovered)
        / numberOfLines);
  }

  public int getMutationCoverage() {
    return numberOfMutations == 0 ? 100 : Math
        .round((100f * numberOfMutationsDetected) / numberOfMutations);
  }

  public void add(MutationTotals data) {
    this.addClasses(data.getNumberOfClasses());
    this.addLines(data.getNumberOfLines());
    this.addLinesCovered(data.getNumberOfLinesCovered());
    this.addMutations(data.getNumberOfMutations());
    this.addMutationsDetetcted(data.getNumberOfMutationsDetected());
  }

}