package org.pitest.mutationtest.report.html;

public class MutationScore {
  private long numberOfUniqueMutations;
  private long numberOfUniqueMutationsKilled;
  private long numberOfUniqueMutationsDetected;

  public MutationScore() {
    this(0, 0, 0);
  }
  
  public MutationScore(long numberOfUniqueMutations,
                       long numberOfUniqueMutationsKilled, 
                       long numberOfUniqueMutationsDetected) {
    super();
    this.numberOfUniqueMutations = numberOfUniqueMutations;
    this.numberOfUniqueMutationsKilled = numberOfUniqueMutationsKilled;
    this.numberOfUniqueMutationsDetected = numberOfUniqueMutationsDetected;
  }

  public long getNumberOfUniqueMutations() {
    return numberOfUniqueMutations;
  }

  public long getNumberOfUniqueMutationsKilled() {
    return numberOfUniqueMutationsKilled;
  }

  public long getNumberOfUniqueMutationsDetected() {
    return numberOfUniqueMutationsDetected;
  }

  public long getNumberOfUniqueMutationsPossiblyDetected() {
    return numberOfUniqueMutationsDetected - numberOfUniqueMutationsKilled;
  }

  public int getKilledScore() {
    return numberOfUniqueMutations == 0 ? 100
            : Math.round((100f * numberOfUniqueMutationsKilled)
                / numberOfUniqueMutations);
  }

  public int getPossiblyDetectedScore() {
    return numberOfUniqueMutations == 0 ? 100
            : Math.round((100f * getNumberOfUniqueMutationsDetected())
                / numberOfUniqueMutations);
  }

  public void add(MutationScore score) {
    numberOfUniqueMutations += score.numberOfUniqueMutations;
    numberOfUniqueMutationsDetected += score.numberOfUniqueMutationsDetected;
    numberOfUniqueMutationsKilled += score.numberOfUniqueMutationsKilled;
  }
}
