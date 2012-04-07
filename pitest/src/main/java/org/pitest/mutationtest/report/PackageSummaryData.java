package org.pitest.mutationtest.report;

import java.util.ArrayList;
import java.util.List;

public class PackageSummaryData {
  private final MutationTotals totals = new  MutationTotals();
  private String                        packageName;
  private List<MutationTestSummaryData> summaryData               = new ArrayList<MutationTestSummaryData>();

  public PackageSummaryData(String packageName) {
    this.packageName = packageName;
  }

  public void addSummaryData(MutationTestSummaryData data) {
    totals.add(data.getTotals());    
    summaryData.add(data);
  }
  
  public MutationTotals getTotals() {
    return this.totals;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getPackageDirectory() {
    return packageName.replace(".", "_");
  }


  public List<MutationTestSummaryData> getSummaryData() {
    return summaryData;
  }
}