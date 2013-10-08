package org.pitest.mutationtest.tooling;

import org.pitest.coverage.CoverageSummary;
import org.pitest.mutationtest.statistics.MutationStatistics;

public class CombinedStatistics {

  private final MutationStatistics mutationStatistics;
  private final CoverageSummary    coverageSummary;

  public CombinedStatistics(final MutationStatistics mutationStatistics,
      final CoverageSummary coverageSummary) {
    this.mutationStatistics = mutationStatistics;
    this.coverageSummary = coverageSummary;
  }

  public MutationStatistics getMutationStatistics() {
    return this.mutationStatistics;
  }

  public CoverageSummary getCoverageSummary() {
    return this.coverageSummary;
  }

}
