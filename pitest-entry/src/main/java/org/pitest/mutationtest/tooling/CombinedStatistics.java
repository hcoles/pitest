package org.pitest.mutationtest.tooling;

import org.pitest.coverage.CoverageSummary;
import org.pitest.mutationtest.statistics.MutationStatistics;

import java.util.List;

public class CombinedStatistics {

  private final MutationStatistics mutationStatistics;
  private final CoverageSummary    coverageSummary;

  private final List<String> issues;

  public CombinedStatistics(MutationStatistics mutationStatistics,
                            CoverageSummary coverageSummary,
                            List<String> issues) {
    this.mutationStatistics = mutationStatistics;
    this.coverageSummary = coverageSummary;
    this.issues = issues;
  }

  public MutationStatistics getMutationStatistics() {
    return this.mutationStatistics;
  }

  public CoverageSummary getCoverageSummary() {
    return this.coverageSummary;
  }

  public List<String> getIssues() {
    return issues;
  }

}
