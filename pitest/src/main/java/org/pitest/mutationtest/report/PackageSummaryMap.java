package org.pitest.mutationtest.report;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class PackageSummaryMap {
  
  private final Map<String, PackageSummaryData> packageSummaryData = new TreeMap<String, PackageSummaryData>();
  
  
  private PackageSummaryData getPackageSummaryData(String packageName) {
    PackageSummaryData psData;
    if (packageSummaryData.containsKey(packageName)) {
      psData = packageSummaryData.get(packageName);
    } else {
      psData = new PackageSummaryData(packageName);
      packageSummaryData.put(packageName, psData);
    }
    return psData;
  }


  public void add(String packageName, MutationTestSummaryData data) {
    getPackageSummaryData(packageName).addSummaryData(data);
  }


  public Collection<PackageSummaryData> values() {
    return packageSummaryData.values();
  }

}
