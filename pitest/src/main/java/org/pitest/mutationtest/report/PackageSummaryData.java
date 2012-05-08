package org.pitest.mutationtest.report;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PackageSummaryData {
  private final MutationTotals                totals      = new MutationTotals();
  private final String                        packageName;
  private final Map<String,MutationTestSummaryData> summaryData = new HashMap<String,MutationTestSummaryData>();

  public PackageSummaryData(final String packageName) {
    this.packageName = packageName;
  }

  public void addSummaryData(final MutationTestSummaryData data) {
    MutationTestSummaryData existing = summaryData.get(data.getClassName());
    if ( existing == null ) {
      this.totals.add(data.getTotals());
      this.summaryData.put(data.getClassName(),data);
    } else {
     totals.addIgnoringLinesAndClasses(data.getTotals()); 
     existing.add(data);
    }

  }

  public MutationTestSummaryData getForSourceFile(String filename) {
    return summaryData.get(filename);
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
    return new ArrayList<MutationTestSummaryData>(this.summaryData.values());
  }
}