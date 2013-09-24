package org.pitest.mutationtest.report.html;

import java.util.Comparator;
import java.util.EnumMap;

import org.pitest.mutationtest.DetectionStatus;
import org.pitest.mutationtest.MutationResult;

import static org.pitest.mutationtest.DetectionStatus.*;

class ResultComparator implements Comparator<MutationResult> {
  
  private final static EnumMap<DetectionStatus,Integer> RANK = new EnumMap<DetectionStatus,Integer>(DetectionStatus.class);
  
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


  public int compare(MutationResult o1, MutationResult o2) {
    return getRanking(o1.getStatus()) - getRanking(o2.getStatus());

  }

  private int getRanking(DetectionStatus status) {
    return RANK.get(status);
  }

}
