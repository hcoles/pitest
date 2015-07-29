package org.pitest.mutationtest.report.html;

import java.io.Serializable;
import java.util.Comparator;

public class MutationTestSummaryDataFileNameComparator implements
Comparator<MutationTestSummaryData>, Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public int compare(final MutationTestSummaryData arg0,
      final MutationTestSummaryData arg1) {
    return arg0.getFileName().compareTo(arg1.getFileName());
  }

}
