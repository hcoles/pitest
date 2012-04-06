package org.pitest.mutationtest.report;

import java.util.ArrayList;
import java.util.List;

public class PackageSummaryData {
  private long                          numberOfClasses           = 0;
  private long                          numberOfLines             = 0;
  private long                          numberOfLinesCovered      = 0;
  private long                          numberOfMutations         = 0;
  private long                          numberOfMutationsDetected = 0;
  private String                        packageName;
  private List<MutationTestSummaryData> summaryData               = new ArrayList<MutationTestSummaryData>();

  public PackageSummaryData(String packageName) {
    this.packageName = packageName;
  }

  public void addSummaryData(MutationTestSummaryData data) {
   
    this.addClasses(1); // ignores nested classes
    this.addLines(data.getNumberOfLines());
    this.addLinesCovered(data.getNumberOfLinesCovered());
    this.addMutations(data.getNumberOfMutations());
    this.addMutationsDetetcted(data.getNumberOfMutationsDetected());
    
    summaryData.add(data);
  }
  
  public long getNumberOfClasses() {
    return numberOfClasses;
  }

  private void addClasses(long classes) {
    this.numberOfClasses += classes;
  }

  public long getNumberOfLines() {
    return numberOfLines;
  }

  private void addLines(long lines) {
    this.numberOfLines += lines;
  }

  public long getNumberOfLinesCovered() {
    return numberOfLinesCovered;
  }

  private void addLinesCovered(long linesCovered) {
    this.numberOfLinesCovered += linesCovered;
  }

  public long getNumberOfMutations() {
    return numberOfMutations;
  }

  private void addMutations(long mutations) {
    this.numberOfMutations += mutations;
  }

  public long getNumberOfMutationsDetected() {
    return numberOfMutationsDetected;
  }

  private void addMutationsDetetcted(long mutationsKilled) {
    this.numberOfMutationsDetected += mutationsKilled;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getPackageDirectory() {
    return packageName.replace(".", "_");
  }

  public int getLineCoverage() {
    return numberOfLines == 0 ? 100 : Math.round((100f * numberOfLinesCovered)
        / numberOfLines);
  }

  public int getMutationCoverage() {
    return numberOfMutations == 0 ? 100 : Math
        .round((100f * numberOfMutationsDetected) / numberOfMutations);
  }

  public List<MutationTestSummaryData> getSummaryData() {
    return summaryData;
  }
}