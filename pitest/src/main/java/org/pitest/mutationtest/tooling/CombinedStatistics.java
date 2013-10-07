package org.pitest.mutationtest.tooling;

import org.pitest.coverage.CoverageSummary;
import org.pitest.mutationtest.statistics.MutationStatistics;

public class CombinedStatistics {

  private final MutationStatistics mutationStatistics;
  private final CoverageSummary coverageSummary;
  
  public CombinedStatistics(MutationStatistics mutationStatistics, CoverageSummary coverageSummary)  {
    this.mutationStatistics = mutationStatistics;
    this.coverageSummary = coverageSummary;
  }

  public MutationStatistics getMutationStatistics() {
    return mutationStatistics;
  }

  public CoverageSummary getCoverageSummary() {
    return coverageSummary;
  }
  
  
  
}
