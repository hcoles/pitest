package org.pitest.mutationtest.report.html;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PackageSummaryData implements Comparable<PackageSummaryData> {

  private final String                               packageName;
  private final Map<String, MutationTestSummaryData> fileNameToSummaryData = new HashMap<>();

  public PackageSummaryData(final String packageName) {
    this.packageName = packageName;
  }

  public void addSummaryData(final MutationTestSummaryData data) {
    final MutationTestSummaryData existing = this.fileNameToSummaryData
        .get(data.getFileName());
    if (existing == null) {
      this.fileNameToSummaryData.put(data.getFileName(), data);
    } else {
      existing.add(data);
    }

  }

  public MutationTestSummaryData getForSourceFile(final String filename) {
    return this.fileNameToSummaryData.get(filename);
  }

  public MutationTotals getTotals() {
    final MutationTotals mt = new MutationTotals();
    for (final MutationTestSummaryData each : this.fileNameToSummaryData
        .values()) {
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
    final ArrayList<MutationTestSummaryData> values = new ArrayList<>(
        this.fileNameToSummaryData.values());
    values.sort(new MutationTestSummaryDataFileNameComparator());
    return values;
  }

  @Override
  public int hashCode() {
    return Objects.hash(packageName);
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || getClass() != obj.getClass()) {
      return false;
    }
    final PackageSummaryData other = (PackageSummaryData) obj;
    return Objects.equals(packageName, other.packageName);
  }

  @Override
  public int compareTo(final PackageSummaryData arg0) {
    return this.packageName.compareTo(arg0.packageName);
  }
}