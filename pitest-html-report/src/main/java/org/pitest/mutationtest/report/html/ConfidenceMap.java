package org.pitest.mutationtest.report.html;

import java.util.EnumSet;

import org.pitest.mutationtest.DetectionStatus;

class ConfidenceMap {

  private static final EnumSet<DetectionStatus> HIGH = EnumSet
      .of(DetectionStatus.KILLED,
          DetectionStatus.SURVIVED,
          DetectionStatus.NO_COVERAGE,
          DetectionStatus.NON_VIABLE);

  public static boolean hasHighConfidence(final DetectionStatus status) {
    return HIGH.contains(status);
  }

}
