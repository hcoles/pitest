package org.pitest.mutationtest.report.html;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;
import org.pitest.mutationtest.report.html.MutationTotals;

public class MutationTotalsTest {

  private MutationTotals testee;

  @Before
  public void setUp() {
    this.testee = new MutationTotals();
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenNoLinesPresent() {
    assertEquals(100, this.testee.getLineCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenNoLinesCovered() {
    this.testee.addLines(100);
    assertEquals(0, this.testee.getLineCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenAllLinesCovered() {
    this.testee.addLines(100);
    this.testee.addLinesCovered(100);
    assertEquals(100, this.testee.getLineCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateLineCoverageWhenPartiallyCovered() {
    this.testee.addLines(63);
    this.testee.addLinesCovered(20);
    assertEquals(32, this.testee.getLineCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateMutationCoverageWhenNoMutationsPresent() {
    assertEquals(100, this.testee.getMutationCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateMutationCoverageWhenNoMutationsDetected() {
    this.testee.addMutations(100);
    assertEquals(0, this.testee.getMutationCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateMutationsCoverageWhenAllMutationsDetected() {
    this.testee.addMutations(100);
    this.testee.addMutationsDetetcted(100);
    assertEquals(100, this.testee.getMutationCoverage());
  }

  @Test
  public void shouldCorrectlyCalculateMutationCoverageWhenSomeMutationUndetected() {
    this.testee.addMutations(63);
    this.testee.addMutationsDetetcted(20);
    assertEquals(32, this.testee.getMutationCoverage());
  }

  @Test
  public void shouldAccumulateAddedValues() {
    final MutationTotals extra = new MutationTotals();
    extra.addFiles(2);
    extra.addLines(8);
    extra.addLinesCovered(4);
    extra.addMutations(9);
    extra.addMutationsDetetcted(3);
    this.testee.add(extra);
    assertEquals(2, this.testee.getNumberOfFiles());
    assertEquals(50, this.testee.getLineCoverage());
    assertEquals(33, this.testee.getMutationCoverage());
  }

}
