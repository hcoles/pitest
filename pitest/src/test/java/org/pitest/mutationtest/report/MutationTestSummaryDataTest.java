package org.pitest.mutationtest.report;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.classinfo.ClassInfo;
import org.pitest.mutationtest.results.MutationResult;


public class MutationTestSummaryDataTest {
  
  private MutationTestSummaryData testee;

  private final String FILE_NAME = "foo.java";
  
  @Test
  public void shouldReturnCorrectNumberOfFilesWhenAnalysedInOneUnit() {
    final ClassInfo clazz = makeClass();
    testee = buildSummaryData(clazz);
    assertEquals(1, testee.getTotals().getNumberOfFiles());
  }
  
  @Test
  public void shouldReturnCorrectNumberOfLinesWhenAnalysedInOneUnit() {
    final int lines = 100;
    final ClassInfo clazz = makeClass(lines);
    testee = buildSummaryData(clazz);
    assertEquals(lines, testee.getTotals().getNumberOfLines());
  }
  
  @Test
  public void shouldReturnCorrectNumberOfCoveredLinesWhenAnalysedInOneUnit() {
    final int linesCovered = 100;
    final ClassInfo clazz = makeClass(200);
    testee = buildSummaryData(clazz, linesCovered);
    assertEquals(linesCovered, testee.getTotals().getNumberOfLinesCovered());
  }
  

  @Test
  public void shouldReturnCorrectNumberOfFilesWhenOneClassAnalysedInTwoUnits() {
    final ClassInfo clazz = makeClass();
    testee = buildSummaryData(clazz);
    MutationTestSummaryData additonalDataForSameClass = buildSummaryData(clazz);
    testee.add(additonalDataForSameClass);
    assertEquals(1, testee.getTotals().getNumberOfFiles());
  }
  
  @Test
  public void shouldReturnCorrectNumberOfLinesWhenAnalysedInTwoUnit() {
    final int lines = 100;
    final ClassInfo clazz = makeClass(lines);
    testee = buildSummaryData(clazz);
    MutationTestSummaryData additonalDataForSameClass = buildSummaryData(clazz);
    testee.add(additonalDataForSameClass);
    assertEquals(lines, testee.getTotals().getNumberOfLines());
  }
  
  @Test
  public void shouldReturnCorrectNumberOfCoveredLinesWhenAnalysedInTwoUnits() {
    final int linesCovered = 100;
    final ClassInfo clazz = makeClass(200);
    testee = buildSummaryData(clazz,linesCovered);
    MutationTestSummaryData additonalDataForSameClass = buildSummaryData(clazz,linesCovered);
    testee.add(additonalDataForSameClass);
    assertEquals(linesCovered, testee.getTotals().getNumberOfLinesCovered());
  }
    
  @Test
  public void shouldReturnCorrectNumberOfLinesWhenCombiningResultsForDifferentClasses() {
    testee = buildSummaryData( makeClass(100));
    MutationTestSummaryData addiitonalDataForSameClass = buildSummaryData( makeClass(200));
    testee.add(addiitonalDataForSameClass);
    assertEquals(100 + 200, testee.getTotals().getNumberOfLines());
  }
  
  @Test
  public void shouldReturnCorrectNumberOfLinesCoveredWhenCombiningResultsForDifferentClasses() {
    testee = buildSummaryData( makeClass(100), 300);
    MutationTestSummaryData addiitonalDataForSameClass = buildSummaryData( makeClass(200), 100);
    testee.add(addiitonalDataForSameClass);
    assertEquals(300 + 100, testee.getTotals().getNumberOfLinesCovered());
  }

  private ClassInfo makeClass( ) {
    return makeClass(100);
  }
  
  private ClassInfo makeClass(int numberOfLines ) {
    ClassInfo clazz =  Mockito.mock(ClassInfo.class);
    when(clazz.getNumberOfCodeLines()).thenReturn(numberOfLines);
    return clazz;
  }
  
  private MutationTestSummaryData buildSummaryData(ClassInfo clazz) {
    return buildSummaryData(clazz,0);
  }
  
  private MutationTestSummaryData buildSummaryData(ClassInfo clazz, int linesCovered) {
    Collection<ClassInfo> classes = Arrays.asList(clazz);
    Collection<MutationResult> results = Collections.emptyList();
    Collection<String> mutators = Collections.emptyList();
    return new MutationTestSummaryData(FILE_NAME, results, mutators, classes, linesCovered);
  }
  
  
}
