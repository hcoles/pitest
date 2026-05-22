package org.pitest.mutationtest.report.html;


import static org.assertj.core.api.Assertions.assertThat;
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
        make(DetectionStatus.NO_COVERAGE), make(DetectionStatus.KILLED),
            make(DetectionStatus.EQUIVALENT));
    mrs.sort(this.testee);

    assertThat(mrs)
        .containsExactly(
            make(DetectionStatus.SURVIVED),
            make(DetectionStatus.NO_COVERAGE),
            make(DetectionStatus.TIMED_OUT),
            make(DetectionStatus.EQUIVALENT),
            make(DetectionStatus.KILLED)
        );

  }

  private MutationResult make(final DetectionStatus status) {
    return new MutationResult(null, MutationStatusTestPair.notAnalysed(0, status, Collections.emptyList()));
  }

}
