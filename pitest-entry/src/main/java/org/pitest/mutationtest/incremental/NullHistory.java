package org.pitest.mutationtest.incremental;

import java.util.Collections;
import java.util.List;

import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationDetails;
public class NullHistory implements History {

  @Override
  public void initialize() {

  }

  @Override
  public void processCoverage(CoverageDatabase coverageData) {

  }

  @Override
  public void recordResult(final MutationResult result) {

  }

  @Override
  public List<MutationResult> analyse(List<MutationDetails> mutationsForClasses) {
    return Collections.emptyList();
  }

  @Override
  public void close() {

  }

}
