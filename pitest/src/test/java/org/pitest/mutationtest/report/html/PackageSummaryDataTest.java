package org.pitest.mutationtest.report.html;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.classinfo.ClassInfo;
import org.pitest.mutationtest.report.MutationTestSummaryData;
import org.pitest.mutationtest.results.MutationResult;


public class PackageSummaryDataTest {
  
  @Test
  public void shouldReturnSummaryDataInAlphabeticOrder() {
    PackageSummaryData testee = new PackageSummaryData("foo");
    MutationTestSummaryData a = makeSummaryData("a");
    MutationTestSummaryData z = makeSummaryData("z");
    testee.addSummaryData(z);
    testee.addSummaryData(a);
    assertEquals(Arrays.asList(a,z), testee.getSummaryData());
    
  }
  
  @Test
  public void shouldSortByPackageName() {
    PackageSummaryData aa = new PackageSummaryData("aa");
    PackageSummaryData ab = new PackageSummaryData("ab");
    PackageSummaryData c = new PackageSummaryData("c");
    List<PackageSummaryData> actual = Arrays.asList(c,aa,ab);
    Collections.sort(actual);
    assertEquals(Arrays.asList(aa,ab,c), actual);
  }
  
  private MutationTestSummaryData makeSummaryData(String fileName) {
    return new MutationTestSummaryData(fileName,
        Collections.<MutationResult> emptyList(),
        Collections.<String> emptyList(), Collections.<ClassInfo> emptyList(),
        0);
  }

}
