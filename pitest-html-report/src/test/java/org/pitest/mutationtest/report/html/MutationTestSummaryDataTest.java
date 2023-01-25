package org.pitest.mutationtest.report.html;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.ClassLines;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.config.Mutator;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.pitest.mutationtest.LocationMother.aMutationId;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

public class MutationTestSummaryDataTest {

  private MutationTestSummaryData testee;

  private static final String     FILE_NAME = "foo.java";

  @Test
  public void shouldReturnCorrectNumberOfFilesWhenAnalysedInOneUnit() {
    final ClassLines clazz = makeClass();
    this.testee = buildSummaryData(clazz);
    assertEquals(1, this.testee.getTotals().getNumberOfFiles());
  }

  @Test
  public void shouldReturnCorrectNumberOfLinesWhenAnalysedInOneUnit() {
    final int lines = 4;
    final ClassLines clazz = makeClass(lines);
    this.testee = buildSummaryData(clazz);
    assertThat(this.testee.getTotals().getNumberOfLines()).isEqualTo(lines);
  }

  @Test
  public void shouldReturnCorrectNumberOfCoveredLinesWhenAnalysedInOneUnit() {
    final int linesCovered = 100;
    final ClassLines clazz = makeClass(200);
    this.testee = buildSummaryData(clazz, linesCovered);
    assertEquals(linesCovered, this.testee.getTotals()
        .getNumberOfLinesCovered());
  }

  @Test
  public void shouldReturnCorrectNumberOfFilesWhenOneClassAnalysedInTwoUnits() {
    final ClassLines clazz = makeClass();
    this.testee = buildSummaryData(clazz);
    final MutationTestSummaryData additonalDataForSameClass = buildSummaryData(clazz);
    this.testee.add(additonalDataForSameClass);
    assertEquals(1, this.testee.getTotals().getNumberOfFiles());
  }

  @Test
  public void shouldReturnCorrectNumberOfLinesWhenAnalysedInTwoUnit() {
    final int lines = 100;
    final ClassLines clazz = makeClass(lines);
    this.testee = buildSummaryData(clazz);
    final MutationTestSummaryData additonalDataForSameClass = buildSummaryData(clazz);
    this.testee.add(additonalDataForSameClass);
    assertEquals(lines, this.testee.getTotals().getNumberOfLines());
  }

  @Test
  public void shouldReturnCorrectNumberOfCoveredLinesWhenAnalysedInTwoUnits() {
    final int linesCovered = 100;
    final ClassLines clazz = makeClass(200);
    this.testee = buildSummaryData(clazz, linesCovered);
    final MutationTestSummaryData additonalDataForSameClass = buildSummaryData(
        clazz, linesCovered);
    this.testee.add(additonalDataForSameClass);
    assertEquals(linesCovered, this.testee.getTotals()
        .getNumberOfLinesCovered());
  }

  @Test
  public void shouldReturnCorrectNumberOfLinesWhenCombiningResultsForDifferentClasses() {
    this.testee = buildSummaryData(makeClass("foo",100));
    final MutationTestSummaryData additionalDataForSameClass = buildSummaryData(makeClass("bar", 200));
    this.testee.add(additionalDataForSameClass);
    assertThat(this.testee.getTotals().getNumberOfLines()).isEqualTo(100 + 200);
  }

  @Test
  public void shouldReturnCorrectNumberOfLinesCoveredWhenCombiningResultsForDifferentClasses() {
    this.testee = buildSummaryData(makeClass("foo",100), 20);
    final MutationTestSummaryData additionalDataForSameClass = buildSummaryData(
        makeClass("bar",200), 100);
    this.testee.add(additionalDataForSameClass);
    assertEquals(20 + 100, this.testee.getTotals().getNumberOfLinesCovered());
  }

  @Test
  public void shouldReturnCorrectTestStrengthWhenAnalysedInOneUnit() {
    this.testee = buildSummaryDataWithMutationResults(makeClass(),
            aMutationResult(DetectionStatus.NO_COVERAGE, "a"),
            aMutationResult(DetectionStatus.KILLED, "b"),
            aMutationResult(DetectionStatus.SURVIVED, "c")
    );
    assertEquals(50, this.testee.getTotals().getTestStrength());
  }

  @Test
  public void shouldReturnCorrectTestStrengthWhenAnalysedInTwoUnits() {
    ClassLines clazz = makeClass();
    this.testee = buildSummaryDataWithMutationResults(clazz,
            aMutationResult(DetectionStatus.NO_COVERAGE, "a"),
            aMutationResult(DetectionStatus.KILLED, "b"),
            aMutationResult(DetectionStatus.SURVIVED, "c")
    );
    final MutationTestSummaryData additionalData = buildSummaryDataWithMutationResults(clazz,
            aMutationResult(DetectionStatus.KILLED, "d"),
            aMutationResult(DetectionStatus.KILLED, "e")
    );
    this.testee.add(additionalData);
    assertEquals(75, this.testee.getTotals().getTestStrength());
  }

  @Test
  public void shouldReturnCorrectTestStrengthWhenWhenCombiningResultsForDifferentClasses() {
    this.testee = buildSummaryDataWithMutationResults(makeClass(100),
            aMutationResult(DetectionStatus.NO_COVERAGE, "a"),
            aMutationResult(DetectionStatus.KILLED, "b"),
            aMutationResult(DetectionStatus.SURVIVED, "c")
    );
    final MutationTestSummaryData additionalData = buildSummaryDataWithMutationResults(makeClass(200),
            aMutationResult(DetectionStatus.KILLED, "d"),
            aMutationResult(DetectionStatus.KILLED, "e")
    );
    this.testee.add(additionalData);
    assertEquals(75, this.testee.getTotals().getTestStrength());
  }

  @Test
  public void shouldReturnSortedListOfMutators() {
    this.testee = buildSummaryDataMutators();

    TreeSet<Object> sortedSet = Mutator.all().stream()
            .map(MethodMutatorFactory::getName)
            .collect(TreeSet::new, TreeSet::add, TreeSet::addAll);

    assertEquals(sortedSet, this.testee.getMutators());
  }

  private ClassLines makeClass() {
    return makeClass(100);
  }

  private ClassLines makeClass(final int numberOfLines) {
    return makeClass("foo", numberOfLines);
  }

  private ClassLines makeClass(String name, int numberOfLines) {
    return new ClassLines(ClassName.fromString(name), IntStream.range(1, numberOfLines + 1).boxed().collect(Collectors.toSet()));
  }

  private MutationTestSummaryData buildSummaryData(final ClassLines clazz) {
    return buildSummaryData(clazz, 0);
  }

  private MutationTestSummaryData buildSummaryData(ClassLines clazz,
      final int linesCovered) {
    final Collection<ClassLines> classes = Arrays.asList(clazz);
    final Collection<MutationResult> results = Collections.emptyList();
    final Collection<String> mutators = Collections.emptyList();
    return new MutationTestSummaryData(FILE_NAME, results, mutators, classes,
        linesCovered);
  }

  private MutationTestSummaryData buildSummaryDataWithMutationResults(ClassLines clazz, final MutationResult... mutationResults) {
    final Collection<ClassLines> classes = Arrays.asList(clazz);
    final Collection<String> mutators = Collections.emptyList();
    return new MutationTestSummaryData(FILE_NAME, Arrays.asList(mutationResults), mutators, classes,
            100);
  }

  private MutationResult aMutationResult(DetectionStatus status) {
    return new MutationResult(aMutationDetail().build(), new MutationStatusTestPair(1, status, "A test"));
  }

  private MutationResult aMutationResult(DetectionStatus status, String mutator) {
    return new MutationResult(aMutationDetail().withId(aMutationId().withMutator(mutator)).build(), new MutationStatusTestPair(1, status, "A test"));
  }

  private MutationTestSummaryData buildSummaryDataMutators() {
    final Collection<ClassLines> classes = Collections.emptyList();
    final Collection<MutationResult> results = Collections.emptyList();
    final Collection<String> mutators = Mutator.all().stream()
            .map(MethodMutatorFactory::getName)
            .collect(Collectors.toList());
    return new MutationTestSummaryData(FILE_NAME, results, mutators, classes, 0);
  }

}
