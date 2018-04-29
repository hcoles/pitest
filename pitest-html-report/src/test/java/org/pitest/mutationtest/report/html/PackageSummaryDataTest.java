package org.pitest.mutationtest.report.html;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.classinfo.ClassInfo;
import org.pitest.mutationtest.MutationResult;

public class PackageSummaryDataTest {

  @Test
  public void shouldReturnSummaryDataInAlphabeticOrder() {
    final PackageSummaryData testee = new PackageSummaryData("foo");
    final MutationTestSummaryData a = makeSummaryData("a");
    final MutationTestSummaryData z = makeSummaryData("z");
    testee.addSummaryData(z);
    testee.addSummaryData(a);
    assertEquals(Arrays.asList(a, z), testee.getSummaryData());

  }

  @Test
  public void shouldSortByPackageName() {
    final PackageSummaryData aa = new PackageSummaryData("aa");
    final PackageSummaryData ab = new PackageSummaryData("ab");
    final PackageSummaryData c = new PackageSummaryData("c");
    final List<PackageSummaryData> actual = Arrays.asList(c, aa, ab);
    Collections.sort(actual);
    assertEquals(Arrays.asList(aa, ab, c), actual);
  }

  private MutationTestSummaryData makeSummaryData(final String fileName) {
    return new MutationTestSummaryData(fileName,
        Collections.<MutationResult> emptyList(),
        Collections.<String> emptyList(), Collections.<ClassInfo> emptyList(),
        0);
  }

}
