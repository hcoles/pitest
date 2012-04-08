package org.pitest.mutationtest.instrument;

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
import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.execute.MutationStatusTestPair;
import org.pitest.mutationtest.results.DetectionStatus;
import org.pitest.mutationtest.results.MutationResult;


public class MutationStatusMapTest {
  
  private MutationStatusMap testee;
  
  @Mock
  private MutationDetails details;
  
  @Mock
  private MutationDetails detailsTwo;
  
  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    testee = new MutationStatusMap();
  }
  
  @Test
  public void shouldDetectWhenHasUnrunMutations() {
    testee.setStatusForMutation(details, DetectionStatus.NOT_STARTED);
    assertTrue(testee.hasUnrunMutations());
  }
  
  @Test
  public void shouldDetectWhenDoesNotHaveUnrunMutations() {
    testee.setStatusForMutation(details, DetectionStatus.KILLED);
    assertFalse(testee.hasUnrunMutations());
  }
  
  @Test
  public void shouldReturnUnRunMutationsWhenSomePresent() {
    testee.setStatusForMutations(Arrays.asList(details,detailsTwo), DetectionStatus.NOT_STARTED);
    assertThat(testee.getUnrunMutations(), hasItems(details, detailsTwo));
  }
  
  @Test
  public void shouldReturnEmptyListMutationsWhenNoUnrunMutationsPresent() {
    testee.setStatusForMutations(Arrays.asList(details,detailsTwo), DetectionStatus.STARTED);
    assertEquals(Collections.emptyList(),testee.getUnrunMutations());
  }
  
  @Test
  public void shouldReturnUnfinishedMutationsWhenSomePresent() {
    testee.setStatusForMutations(Arrays.asList(details,detailsTwo), DetectionStatus.STARTED);
    assertThat(testee.getUnfinishedRuns(), hasItems(details, detailsTwo));
  }
  
  @Test
  public void shouldReturnEmptyListMutationsWhenNoUnfinishedMutationsPresent() {
    testee.setStatusForMutations(Arrays.asList(details,detailsTwo), DetectionStatus.KILLED);
    assertEquals(Collections.emptyList(),testee.getUnrunMutations());
  }
  
  @Test
  public void shouldCreateResultsForAllMutations() {
    MutationStatusTestPair statusPairOne= new MutationStatusTestPair(42,DetectionStatus.KILLED,"foo");
    MutationResult resultOne = new MutationResult(details, statusPairOne);
    testee.setStatusForMutation(details, statusPairOne );
    
    MutationStatusTestPair statusPairTwo = new MutationStatusTestPair(42,DetectionStatus.RUN_ERROR,"bar");
    MutationResult resultTwo = new MutationResult(detailsTwo, statusPairTwo);
    testee.setStatusForMutation(detailsTwo, statusPairTwo );

    assertThat(testee.createMutationResults(), hasItems(resultOne, resultTwo));
  }

}
