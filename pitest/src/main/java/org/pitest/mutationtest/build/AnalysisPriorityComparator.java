package org.pitest.mutationtest.build;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Comparator to prioritise the order of mutation analysis units.
 *
 * The ones with the most mutations are run first. This should make it less
 * likely that a single thread remains running at the of a run because it has
 * just picked up a large unit.
 *
 */
class AnalysisPriorityComparator implements Comparator<MutationAnalysisUnit>,
Serializable {

  private static final long serialVersionUID = 1L;

  @Override
  public int compare(final MutationAnalysisUnit a, final MutationAnalysisUnit b) {
    return b.priority() - a.priority();
  }

}
