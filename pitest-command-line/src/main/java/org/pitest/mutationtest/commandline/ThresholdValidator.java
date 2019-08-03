package org.pitest.mutationtest.commandline;

import org.pitest.coverage.CoverageSummary;
import org.pitest.mutationtest.statistics.MutationStatistics;

public class ThresholdValidator {

  public void throwErrorIfScoreBelowCoverageThreshold(CoverageSummary stats, int threshold) {
    if ((threshold != 0) && (stats.getCoverage() < threshold)) {
      throw new RuntimeException("Line coverage of " + stats.getCoverage()
                    + " is below threshold of " + threshold);
    }
  }

  public void throwErrorIfScoreBelowMutationThreshold(
          final MutationStatistics stats, final int threshold) {
    if ((threshold != 0) && (stats.getPercentageDetected() < threshold)) {
      throw new RuntimeException("Mutation score of "
              + stats.getPercentageDetected() + " is below threshold of "
              + threshold);
    }
  }

  public void throwErrorIfMoreThanMaxSurvivingMutants(
          final MutationStatistics stats, final long threshold) {
    if ((threshold >= 0)
            && (stats.getTotalSurvivingMutations() > threshold)) {
      throw new RuntimeException("Had "
              + stats.getTotalSurvivingMutations() + " surviving mutants, but only "
              + threshold + " survivors allowed");
    }
  }

}
