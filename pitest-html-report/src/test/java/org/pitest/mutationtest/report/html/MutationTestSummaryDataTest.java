package org.pitest.mutationtest.report.html;

import org.junit.Test;
import org.mockito.Mockito;
import org.pitest.classinfo.ClassInfo;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class MutationTestSummaryDataTest {

  private MutationTestSummaryData testee;

  private static final String     FILE_NAME = "foo.java";

  @Test
  public void shouldReturnCorrectNumberOfFilesWhenAnalysedInOneUnit() {
    final ClassInfo clazz = makeClass();
    this.testee = buildSummaryData(clazz);
    assertEquals(1, this.testee.getTotals().getNumberOfFiles());
  }

  @Test
  public void shouldReturnCorrectNumberOfLinesWhenAnalysedInOneUnit() {
    final int lines = 100;
    final ClassInfo clazz = makeClass(lines);
    this.testee = buildSummaryData(clazz);
    assertEquals(lines, this.testee.getTotals().getNumberOfLines());
  }

  @Test
  public void shouldReturnCorrectNumberOfCoveredLinesWhenAnalysedInOneUnit() {
    final int linesCovered = 100;
    final ClassInfo clazz = makeClass(200);
    this.testee = buildSummaryData(clazz, linesCovered);
    assertEquals(linesCovered, this.testee.getTotals()
        .getNumberOfLinesCovered());
  }

  @Test
  public void shouldReturnCorrectNumberOfFilesWhenOneClassAnalysedInTwoUnits() {
    final ClassInfo clazz = makeClass();
    this.testee = buildSummaryData(clazz);
    final MutationTestSummaryData additonalDataForSameClass = buildSummaryData(clazz);
    this.testee.add(additonalDataForSameClass);
    assertEquals(1, this.testee.getTotals().getNumberOfFiles());
  }

  @Test
  public void shouldReturnCorrectNumberOfLinesWhenAnalysedInTwoUnit() {
    final int lines = 100;
    final ClassInfo clazz = makeClass(lines);
    this.testee = buildSummaryData(clazz);
    final MutationTestSummaryData additonalDataForSameClass = buildSummaryData(clazz);
    this.testee.add(additonalDataForSameClass);
    assertEquals(lines, this.testee.getTotals().getNumberOfLines());
  }

  @Test
  public void shouldReturnCorrectNumberOfCoveredLinesWhenAnalysedInTwoUnits() {
    final int linesCovered = 100;
    final ClassInfo clazz = makeClass(200);
    this.testee = buildSummaryData(clazz, linesCovered);
    final MutationTestSummaryData additonalDataForSameClass = buildSummaryData(
        clazz, linesCovered);
    this.testee.add(additonalDataForSameClass);
    assertEquals(linesCovered, this.testee.getTotals()
        .getNumberOfLinesCovered());
  }

  @Test
  public void shouldReturnCorrectNumberOfLinesWhenCombiningResultsForDifferentClasses() {
    this.testee = buildSummaryData(makeClass(100));
    final MutationTestSummaryData addiitonalDataForSameClass = buildSummaryData(makeClass(200));
    this.testee.add(addiitonalDataForSameClass);
    assertEquals(100 + 200, this.testee.getTotals().getNumberOfLines());
  }

  @Test
  public void shouldReturnCorrectNumberOfLinesCoveredWhenCombiningResultsForDifferentClasses() {
    this.testee = buildSummaryData(makeClass(100), 300);
    final MutationTestSummaryData addiitonalDataForSameClass = buildSummaryData(
        makeClass(200), 100);
    this.testee.add(addiitonalDataForSameClass);
    assertEquals(300 + 100, this.testee.getTotals().getNumberOfLinesCovered());
  }

  @Test
  public void shouldReturnSortedListOfMutators() {
    this.testee = buildSummaryDataMutators();

    TreeSet<Object> sortedSet = Mutator.all().stream()
            .map(MethodMutatorFactory::getName)
            .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);

    assertEquals(sortedSet, this.testee.getMutators());
  }

  private ClassInfo makeClass() {
    return makeClass(100);
  }

  private ClassInfo makeClass(final int numberOfLines) {
    final ClassInfo clazz = Mockito.mock(ClassInfo.class);
    when(clazz.getNumberOfCodeLines()).thenReturn(numberOfLines);
    return clazz;
  }

  private MutationTestSummaryData buildSummaryData(final ClassInfo clazz) {
    return buildSummaryData(clazz, 0);
  }

  private MutationTestSummaryData buildSummaryData(final ClassInfo clazz,
      final int linesCovered) {
    final Collection<ClassInfo> classes = Arrays.asList(clazz);
    final Collection<MutationResult> results = Collections.emptyList();
    final Collection<String> mutators = Collections.emptyList();
    return new MutationTestSummaryData(FILE_NAME, results, mutators, classes,
        linesCovered);
  }

  private MutationTestSummaryData buildSummaryDataMutators() {
    final Collection<ClassInfo> classes = Collections.emptyList();
    final Collection<MutationResult> results = Collections.emptyList();
    final Collection<String> mutators = Mutator.all().stream()
            .map(MethodMutatorFactory::getName)
            .collect(Collectors.toList());
    return new MutationTestSummaryData(FILE_NAME, results, mutators, classes, 0);
  }

}
