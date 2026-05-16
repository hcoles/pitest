package org.pitest.aggregate;

import java.math.BigDecimal;

public class AggregationResult {
  private final long mutations;
  private final long mutationsSurvived;
  private final BigDecimal mutationCoverage;
  private final BigDecimal testStrength;

  public AggregationResult(long mutations, long mutationsSurvived, BigDecimal mutationCoverage, BigDecimal testStrength) {
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

  public BigDecimal getMutationCoverage() {
    return mutationCoverage;
  }

  public BigDecimal getTestStrength() {
    return testStrength;
  }
}
