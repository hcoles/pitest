package org.pitest.mutationtest.build;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.util.Log;

public class KnownStatusMutationTestUnit implements MutationAnalysisUnit {

  private static final Logger        LOG = Log.getLogger();

  private final List<MutationResult> mutations;

  public KnownStatusMutationTestUnit(final List<MutationResult> mutations) {
    this.mutations = mutations;
  }

  @Override
  public MutationMetaData call() throws Exception {
    LOG.fine("Using historic results for " + this.mutations.size()
        + " mutations");
    return new MutationMetaData(this.mutations);

  }

  @Override
  public int priority() {
    return Integer.MAX_VALUE;
  }

  @Override
  public Collection<MutationDetails> mutants() {
    return mutations.stream()
            .map(MutationResult::getDetails)
            .collect(Collectors.toList());
  }

}
