package org.pitest.mutationtest.report.html;

import static org.pitest.mutationtest.DetectionStatus.KILLED;
import static org.pitest.mutationtest.DetectionStatus.MEMORY_ERROR;
import static org.pitest.mutationtest.DetectionStatus.NON_VIABLE;
import static org.pitest.mutationtest.DetectionStatus.NOT_STARTED;
import static org.pitest.mutationtest.DetectionStatus.NO_COVERAGE;
import static org.pitest.mutationtest.DetectionStatus.RUN_ERROR;
import static org.pitest.mutationtest.DetectionStatus.STARTED;
import static org.pitest.mutationtest.DetectionStatus.SURVIVED;
import static org.pitest.mutationtest.DetectionStatus.TIMED_OUT;

import java.io.Serializable;
import java.util.Comparator;
import java.util.EnumMap;

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;

class ResultComparator implements Comparator<MutationResult>, Serializable {

  private static final long                              serialVersionUID = 1L;
  private static final EnumMap<DetectionStatus, Integer> RANK             = new EnumMap<DetectionStatus, Integer>(
                                                                              DetectionStatus.class);

  static {
    RANK.put(KILLED, 4);
    RANK.put(SURVIVED, 0);
    RANK.put(TIMED_OUT, 2);
    RANK.put(NON_VIABLE, 3);
    RANK.put(MEMORY_ERROR, 1);
    RANK.put(NOT_STARTED, 1);
    RANK.put(STARTED, 1);
    RANK.put(RUN_ERROR, 0);
    RANK.put(NO_COVERAGE, 0);
  }

  @Override
  public int compare(MutationResult o1, MutationResult o2) {
    return getRanking(o1.getStatus()) - getRanking(o2.getStatus());

  }

  private int getRanking(DetectionStatus status) {
    return RANK.get(status);
  }

}
