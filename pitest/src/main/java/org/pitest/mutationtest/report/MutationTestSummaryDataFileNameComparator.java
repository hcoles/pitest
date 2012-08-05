package org.pitest.mutationtest.report;

import java.util.Comparator;

public class MutationTestSummaryDataFileNameComparator implements
    Comparator<MutationTestSummaryData> {

  public int compare(MutationTestSummaryData arg0, MutationTestSummaryData arg1) {
    return arg0.getFileName().compareTo(arg1.getFileName());
  }

}
