package org.pitest.mutationtest.report;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.classinfo.ClassInfo;
import org.pitest.mutationtest.report.html.MutationTestSummaryDataFileNameComparator;
import org.pitest.mutationtest.results.MutationResult;

public class MutationTestSummaryDataFileNameComparatorTest {

  @Test
  public void shouldSortDataByFileName() {
    MutationTestSummaryDataFileNameComparator testee = new MutationTestSummaryDataFileNameComparator();
    MutationTestSummaryData ab = makeSummaryData("ab");
    MutationTestSummaryData aa = makeSummaryData("aa");
    MutationTestSummaryData z = makeSummaryData("z");
    List<MutationTestSummaryData> list = Arrays.asList(z, aa, ab);
    Collections.sort(list, testee);
    List<MutationTestSummaryData> expected = Arrays.asList(aa, ab, z);
    assertEquals(expected,list);
  }

  private MutationTestSummaryData makeSummaryData(String fileName) {
    return new MutationTestSummaryData(fileName,
        Collections.<MutationResult> emptyList(),
        Collections.<String> emptyList(), Collections.<ClassInfo> emptyList(),
        0);
  }

}
