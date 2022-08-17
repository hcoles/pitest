package org.pitest.aggregate;

public class AggregationResult {
  private final long mutations;
  private final long mutationsSurvived;
  private final int  mutationCoverage;

  private final int testStrength;

  public AggregationResult(long mutations, long mutationsSurvived, int mutationCoverage, int testStrength) {
    this.mutations = mutations;
    this.mutationsSurvived = mutationsSurvived;
    this.mutationCoverage = mutationCoverage;
    this.testStrength = testStrength;
  }

  public long getMutations() {
    return mutations;
  }

  public long getMutationsSurvived() {
    return mutationsSurvived;
  }

  public int getMutationCoverage() {
    return mutationCoverage;
  }

  public int getTestStrength() {
    return testStrength;
  }
}
