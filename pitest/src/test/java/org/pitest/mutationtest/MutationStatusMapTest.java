package org.pitest.mutationtest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.matchers.JUnitMatchers.hasItems;

import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.pitest.mutationtest.engine.MutationDetails;


public class MutationStatusMapTest {

  private MutationStatusMap testee;

  @Mock
  private MutationDetails   details;

  @Mock
  private MutationDetails   detailsTwo;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    this.testee = new MutationStatusMap();
  }

  @Test
  public void shouldDetectWhenHasUnrunMutations() {
    this.testee.setStatusForMutation(this.details, DetectionStatus.NOT_STARTED);
    assertTrue(this.testee.hasUnrunMutations());
  }

  @Test
  public void shouldDetectWhenDoesNotHaveUnrunMutations() {
    this.testee.setStatusForMutation(this.details, DetectionStatus.KILLED);
    assertFalse(this.testee.hasUnrunMutations());
  }

  @Test
  public void shouldReturnUnRunMutationsWhenSomePresent() {
    this.testee.setStatusForMutations(
        Arrays.asList(this.details, this.detailsTwo),
        DetectionStatus.NOT_STARTED);
    assertThat(this.testee.getUnrunMutations(),
        hasItems(this.details, this.detailsTwo));
  }

  @Test
  public void shouldReturnEmptyListMutationsWhenNoUnrunMutationsPresent() {
    this.testee.setStatusForMutations(
        Arrays.asList(this.details, this.detailsTwo), DetectionStatus.STARTED);
    assertEquals(Collections.emptyList(), this.testee.getUnrunMutations());
  }

  @Test
  public void shouldReturnUnfinishedMutationsWhenSomePresent() {
    this.testee.setStatusForMutations(
        Arrays.asList(this.details, this.detailsTwo), DetectionStatus.STARTED);
    assertThat(this.testee.getUnfinishedRuns(),
        hasItems(this.details, this.detailsTwo));
  }

  @Test
  public void shouldReturnEmptyListMutationsWhenNoUnfinishedMutationsPresent() {
    this.testee.setStatusForMutations(
        Arrays.asList(this.details, this.detailsTwo), DetectionStatus.KILLED);
    assertEquals(Collections.emptyList(), this.testee.getUnrunMutations());
  }

  @Test
  public void shouldCreateResultsForAllMutations() {
    final MutationStatusTestPair statusPairOne = new MutationStatusTestPair(42,
        DetectionStatus.KILLED, "foo");
    final MutationResult resultOne = new MutationResult(this.details,
        statusPairOne);
    this.testee.setStatusForMutation(this.details, statusPairOne);

    final MutationStatusTestPair statusPairTwo = new MutationStatusTestPair(42,
        DetectionStatus.RUN_ERROR, "bar");
    final MutationResult resultTwo = new MutationResult(this.detailsTwo,
        statusPairTwo);
    this.testee.setStatusForMutation(this.detailsTwo, statusPairTwo);

    assertThat(this.testee.createMutationResults(),
        hasItems(resultOne, resultTwo));
  }

  @Test
  public void shouldSetStatusToUncoveredWhenMutationHasNoTests() {
    this.testee.setStatusForMutations(
        Arrays.asList(this.details, this.detailsTwo),
        DetectionStatus.NOT_STARTED);
    this.testee.markUncoveredMutations();
    assertEquals(Collections.emptyList(), this.testee.getUnrunMutations());

    final MutationStatusTestPair statusPairOne = new MutationStatusTestPair(42,
        DetectionStatus.NO_COVERAGE, "foo");
    final MutationResult resultOne = new MutationResult(this.details,
        statusPairOne);
    this.testee.setStatusForMutation(this.details, statusPairOne);

    final MutationStatusTestPair statusPairTwo = new MutationStatusTestPair(42,
        DetectionStatus.NO_COVERAGE, "bar");
    final MutationResult resultTwo = new MutationResult(this.detailsTwo,
        statusPairTwo);
    this.testee.setStatusForMutation(this.detailsTwo, statusPairTwo);

    assertThat(this.testee.createMutationResults(),
        hasItems(resultOne, resultTwo));
  }

}
