package org.pitest.mutationtest.report.html;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationIdentifier;

public final class MutationScoreUtil {
  private MutationScoreUtil() {
    super();
  }
  
  public static Map<MutationIdentifier, Collection<MutationResult>> groupBySameMutationOnSameLines(Collection<MutationResult> mutations) {
    Map<MutationIdentifier, Collection<MutationResult>> grouped = new HashMap<>();
    for (MutationResult each : mutations) {
      Collection<MutationResult> results = grouped.get(each.getDetails().getId());
      if (results == null) {
        results = new ArrayList<>();
        grouped.put(each.getDetails().getId(), results);
      }
      results.add(each);
    }
    return grouped;
  }

}
