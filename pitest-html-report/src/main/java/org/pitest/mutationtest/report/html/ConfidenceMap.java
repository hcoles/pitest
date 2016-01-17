package org.pitest.mutationtest.report.html;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.pitest.mutationtest.DetectionStatus;

class ConfidenceMap {

  private static final Set<DetectionStatus> HIGH = new HashSet<DetectionStatus>
      (Arrays.asList(DetectionStatus.KILLED,
          DetectionStatus.SURVIVED,
          DetectionStatus.NO_COVERAGE,
          DetectionStatus.NON_VIABLE));

  public static boolean hasHighConfidence(final DetectionStatus status) {
    return HIGH.contains(status);
  }

}
