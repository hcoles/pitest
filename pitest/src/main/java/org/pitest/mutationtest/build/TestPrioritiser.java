package org.pitest.mutationtest.build;

import java.util.List;

import org.pitest.coverage.TestInfo;
import org.pitest.mutationtest.engine.MutationDetails;

public interface TestPrioritiser {

  /**
   *
   * @param mutation
   *          Mutation to assign tests to
   * @return List of tests to run against mutant in priority order
   */
  List<TestInfo> assignTests(MutationDetails mutation);

}
