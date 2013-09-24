package org.pitest.mutationtest.report.html;

import java.util.Comparator;


public class MutationTestSummaryDataFileNameComparator implements
    Comparator<MutationTestSummaryData> {

  public int compare(final MutationTestSummaryData arg0,
      final MutationTestSummaryData arg1) {
    return arg0.getFileName().compareTo(arg1.getFileName());
  }

}
