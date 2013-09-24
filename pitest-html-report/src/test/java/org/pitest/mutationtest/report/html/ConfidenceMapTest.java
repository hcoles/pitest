package org.pitest.mutationtest.report.html;

import static org.junit.Assert.*;

import org.junit.Test;
import org.pitest.mutationtest.DetectionStatus;

public class ConfidenceMapTest {
  
  @Test
  public void shouldHaveHighConfidenceForKilled() {
    assertTrue(ConfidenceMap.hasHighConfidence(DetectionStatus.KILLED));
  }
  
  @Test
  public void shouldHaveHighConfidenceForSurvived() {
    assertTrue(ConfidenceMap.hasHighConfidence(DetectionStatus.SURVIVED));
  }
  
  @Test
  public void shouldHaveHighConfidenceForNonViable() {
    assertTrue(ConfidenceMap.hasHighConfidence(DetectionStatus.NON_VIABLE));
  }
  
  @Test
  public void shouldHaveHighConfidenceForNoCoverage() {
    assertTrue(ConfidenceMap.hasHighConfidence(DetectionStatus.NO_COVERAGE));
  }
  
  @Test
  public void shouldHaveLowConfidenceForTimedOut() {
    assertFalse(ConfidenceMap.hasHighConfidence(DetectionStatus.TIMED_OUT));
  }
  
  @Test
  public void shouldHaveLowConfidenceForMemoryError() {
    assertFalse(ConfidenceMap.hasHighConfidence(DetectionStatus.MEMORY_ERROR));
  }
  
  @Test
  public void shouldHaveLowConfidenceForNotStarted() {
    assertFalse(ConfidenceMap.hasHighConfidence(DetectionStatus.NOT_STARTED));
  }
  
  @Test
  public void shouldHaveLowConfidenceForStarted() {
    assertFalse(ConfidenceMap.hasHighConfidence(DetectionStatus.STARTED));
  }
  
  @Test
  public void shouldHaveLowConfidenceForRunError() {
    assertFalse(ConfidenceMap.hasHighConfidence(DetectionStatus.RUN_ERROR));
  }

}
