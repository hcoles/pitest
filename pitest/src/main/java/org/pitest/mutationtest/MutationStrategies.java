package org.pitest.mutationtest;

import org.pitest.coverage.CoverageGenerator;
import org.pitest.mutationtest.incremental.HistoryStore;
import org.pitest.mutationtest.verify.BuildVerifier;

public class MutationStrategies {

  private final HistoryStore history;
  private final CoverageGenerator coverage;
  private final ListenerFactory listenerFactory;
  private final BuildVerifier buildVerifier;
  
  public MutationStrategies( HistoryStore history, CoverageGenerator coverage, ListenerFactory listenerFactory, BuildVerifier buildVerifier) {
    this.history = history;
    this.coverage = coverage;
    this.listenerFactory = listenerFactory;
    this.buildVerifier = buildVerifier;
  }

  public HistoryStore history() {
    return history;
  }

  public CoverageGenerator coverage() {
    return coverage;
  }

  public ListenerFactory listenerFactory() {
    return listenerFactory;
  }

  public BuildVerifier buildVerifier() {
    return buildVerifier;
  }

}
