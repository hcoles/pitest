package org.pitest.coverage;

import org.junit.Test;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.Location;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class CoverageResultTest {

  private CoverageResult testee;

  @Test
  public void shouldCalculateCorrectNumberOfCoveredBlocksWhenNoneCovered() {
    this.testee = new CoverageResult(null, 0, true,
        Collections.<BlockLocation> emptyList());
    assertEquals(0, this.testee.getNumberOfCoveredBlocks());
  }

  @Test
  public void shouldCalculateCorrectNumberOfCoveredBlocksWhenOneClassHasCoverage() {
    this.testee = new CoverageResult(null, 0, true,
        Collections.singletonList(makeCoverage("foo", 1)));
    assertEquals(1, this.testee.getNumberOfCoveredBlocks());
  }

  @Test
  public void shouldCalculateCorrectNumberOfCoveredBlocksWhenMultiplesClassesHaveCoverage() {
    this.testee = new CoverageResult(null, 0, true, Arrays.asList(
        makeCoverage("foo", 42), makeCoverage("bar", 42)));
    assertEquals(2, this.testee.getNumberOfCoveredBlocks());
  }

  private BlockLocation makeCoverage(final String name, final int block) {
    final Location l = Location.location(ClassName.fromString(name),
        "amethod", "methodDesc");
    return new BlockLocation(l, block);
  }

}
