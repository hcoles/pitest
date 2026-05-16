package org.pitest.aggregate;

import org.pitest.mutationtest.ClassMutationResults;
import org.pitest.mutationtest.MutationResultListener;
import org.pitest.mutationtest.report.html.MutationTotals;

public class ReportAggregatorResultListener implements MutationResultListener {
  MutationTotals totals = new MutationTotals();
  private final int thresholdPrecision;

  public ReportAggregatorResultListener(int thresholdPrecision) {
    this.thresholdPrecision = thresholdPrecision;
  }

  @Override
  public void runStart() {
  }

  @Override
  public void handleMutationResult(ClassMutationResults results) {
    totals.addFiles(1);
    totals.addMutations(results.getMutations().size());
    totals.addMutationsDetetcted(results.getMutations().stream().filter(mutation -> mutation.getStatus().isDetected()).count());
    totals.addMutationsWithCoverage(results.getMutations().stream().filter(it -> it.getStatus().hasCoverage()).count());
  }

  @Override
  public void runEnd() {
  }

  public AggregationResult result() {
    return new AggregationResult(totals.getNumberOfMutations(), totals.getNumberOfMutations() - totals.getNumberOfMutationsDetected(),
        totals.getMutationCoverage(thresholdPrecision), totals.getTestStrength(thresholdPrecision));
  }
}
