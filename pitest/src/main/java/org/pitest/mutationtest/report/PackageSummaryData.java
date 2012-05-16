package org.pitest.mutationtest.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageSummaryData {

  private final String                               packageName;
  private final Map<String, MutationTestSummaryData> summaryData = new HashMap<String, MutationTestSummaryData>();

  public PackageSummaryData(final String packageName) {
    this.packageName = packageName;
  }

  public void addSummaryData(final MutationTestSummaryData data) {
    final MutationTestSummaryData existing = this.summaryData.get(data
        .getClassName());
    if (existing == null) {
      this.summaryData.put(data.getClassName(), data);
    } else {
      existing.add(data);
    }

  }

  public MutationTestSummaryData getForSourceFile(final String filename) {
    return this.summaryData.get(filename);
  }

  public MutationTotals getTotals() {
    final MutationTotals mt = new MutationTotals();
    for (final MutationTestSummaryData each : this.summaryData.values()) {
      mt.add(each.getTotals());
    }
    return mt;
  }

  public String getPackageName() {
    return this.packageName;
  }

  public String getPackageDirectory() {
    return this.packageName;
  }

  public List<MutationTestSummaryData> getSummaryData() {
    return new ArrayList<MutationTestSummaryData>(this.summaryData.values());
  }
}