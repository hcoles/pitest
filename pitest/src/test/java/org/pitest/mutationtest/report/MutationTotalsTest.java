package org.pitest.mutationtest.report;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;


public class MutationTotalsTest {
  
  private MutationTotals testee;
  
  @Before
  public void setUp() {
    testee = new MutationTotals();
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenNoLinesPresent() {
    assertEquals(100,testee.getLineCoverage());
  }
  
  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenNoLinesCovered() {
    testee.addLines(100);
    assertEquals(0,testee.getLineCoverage());
  }
  
  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenAllLinesCovered() {
    testee.addLines(100);
    testee.addLinesCovered(100);
    assertEquals(100,testee.getLineCoverage());
  }
  
  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenPartiallyCovered() {
    testee.addLines(63);
    testee.addLinesCovered(20);
    assertEquals(32,testee.getLineCoverage());
  }
  
  @Test
  public void shouldCorrectlyCalculateMutationCoverageWhenNoMutationsPresent() {
    assertEquals(100,testee.getMutationCoverage());
  }
  
  @Test
  public void shouldCorrectlyCalculateMutationCoverageWhenNoMutationsDetected() {
    testee.addMutations(100);
    assertEquals(0,testee.getMutationCoverage());
  }
  
  @Test
  public void shouldCorrectlyCalculateMutationsCoverageWhenAllMutationsDetected() {
    testee.addMutations(100);
    testee.addMutationsDetetcted(100);
    assertEquals(100,testee.getMutationCoverage());
  }
  
  @Test
  public void shouldCorrectlyCalculateMutationCoverageWhenSomeMutationUndetected() {
    testee.addMutations(63);
    testee.addMutationsDetetcted(20);
    assertEquals(32,testee.getMutationCoverage());
  }
  
  @Test
  public void shouldAccumulateAddedValues() {
    MutationTotals extra = new MutationTotals();
    extra.addFiles(2);
    extra.addLines(8);
    extra.addLinesCovered(4);
    extra.addMutations(9);
    extra.addMutationsDetetcted(3);
    testee.add(extra);
    assertEquals(2,testee.getNumberOfFiles());
    assertEquals(50,testee.getLineCoverage());
    assertEquals(33,testee.getMutationCoverage());
  }
  
}
