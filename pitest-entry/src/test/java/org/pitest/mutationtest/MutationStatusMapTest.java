package org.pitest.mutationtest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.pitest.mutationtest.LocationMother.aMutationId;
import static org.pitest.mutationtest.engine.MutationDetailsMother.aMutationDetail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.LocationMother.MutationIdentifierBuilder;
import org.pitest.mutationtest.engine.MutationDetails;

public class MutationStatusMapTest {

  private MutationStatusMap testee;

  private MutationDetails   details;

  private MutationDetails   detailsTwo;

  private MutationDetails   aSurvivedMutationDetails;

  @Before
  public void setUp() {
    this.testee = new MutationStatusMap();
    final MutationIdentifierBuilder id = aMutationId().withIndex(1);
    this.details = aMutationDetail().withId(id.withIndex(1)).build();
    this.detailsTwo = aMutationDetail().withId(id.withIndex(2)).build();

    TestInfo fooTest = new TestInfo("foo", "foo", 0, Optional.ofNullable(ClassName.fromString("com.foo")), 0);
    TestInfo barTest = new TestInfo("bar", "bar", 0, Optional.ofNullable(ClassName.fromString("com.foo")), 0);
    this.aSurvivedMutationDetails = aMutationDetail().withId(id.withIndex(3)).withTestsInOrder(Lists.asList(fooTest, new TestInfo[]{barTest})).build();
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
    assertThat(this.testee.getUnrunMutations()).contains(this.details,
        this.detailsTwo);
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
    assertThat(this.testee.getUnfinishedRuns()).contains(this.details,
        this.detailsTwo);
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

    assertThat(this.testee.createMutationResults()).contains(resultOne,
        resultTwo);
  }

  @Test
  public void shouldCreateResultsForSurvivedMutations(){
    final MutationStatusTestPair statusPairOne = new MutationStatusTestPair(42,
            DetectionStatus.SURVIVED, Collections.singletonList("foo"),Arrays.asList("foo1","bar"), Arrays.asList("foo","foo1","bar"));
    this.testee.setStatusForMutation(this.aSurvivedMutationDetails, statusPairOne);

    assertEquals(DetectionStatus.SURVIVED, this.testee.createMutationResults().get(0).getStatus());
    assertThat(this.testee.createMutationResults().get(0).getKillingTests()).contains("foo");
    assertThat(this.testee.createMutationResults().get(0).getSucceedingTests()).contains("foo1","bar");
    assertThat(this.testee.createMutationResults().get(0).getCoveringTests()).contains("foo","foo1","bar");

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

    assertThat(this.testee.createMutationResults()).contains(resultOne,
        resultTwo);
  }

}
