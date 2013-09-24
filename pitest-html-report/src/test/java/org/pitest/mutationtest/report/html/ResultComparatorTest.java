package org.pitest.mutationtest.report.html;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.Test;
import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.MutationStatusTestPair;

public class ResultComparatorTest {

  private final ResultComparator testee = new ResultComparator();

  @Test
  public void shouldSortInDesiredOrder() {
    final List<MutationResult> mrs = Arrays.asList(
        make(DetectionStatus.TIMED_OUT), make(DetectionStatus.SURVIVED),
        make(DetectionStatus.NO_COVERAGE), make(DetectionStatus.KILLED));
    Collections.sort(mrs, this.testee);
    assertEquals(DetectionStatus.SURVIVED,mrs.get(0).getStatus());
    assertEquals(DetectionStatus.NO_COVERAGE,mrs.get(1).getStatus());
    assertEquals(DetectionStatus.TIMED_OUT,mrs.get(2).getStatus());
    assertEquals(DetectionStatus.KILLED,mrs.get(3).getStatus());
  }

  private MutationResult make(final DetectionStatus status) {
    return new MutationResult(null, new MutationStatusTestPair(0, status));
  }

}
