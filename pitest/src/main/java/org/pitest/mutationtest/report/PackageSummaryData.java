package org.pitest.mutationtest.report;

import java.util.ArrayList;
import java.util.List;

public class PackageSummaryData {
  private final MutationTotals                totals      = new MutationTotals();
  private final String                        packageName;
  private final List<MutationTestSummaryData> summaryData = new ArrayList<MutationTestSummaryData>();

  public PackageSummaryData(final String packageName) {
    this.packageName = packageName;
  }

  public void addSummaryData(final MutationTestSummaryData data) {
    this.totals.add(data.getTotals());
    this.summaryData.add(data);
  }

  public MutationTotals getTotals() {
    return this.totals;
  }

  public String getPackageName() {
    return this.packageName;
  }

  public String getPackageDirectory() {
    return this.packageName;
  }

  public List<MutationTestSummaryData> getSummaryData() {
    return this.summaryData;
  }
}