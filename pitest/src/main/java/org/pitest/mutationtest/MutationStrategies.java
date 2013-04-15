package org.pitest.mutationtest;

import org.pitest.coverage.CoverageGenerator;
import org.pitest.mutationtest.incremental.HistoryStore;
import org.pitest.mutationtest.verify.BuildVerifier;
import org.pitest.mutationtest.verify.DefaultBuildVerifier;

public class MutationStrategies {

  private final HistoryStore          history;
  private final CoverageGenerator     coverage;
  private final ListenerFactory       listenerFactory;
  private final BuildVerifier         buildVerifier;
  private final MutationEngineFactory factory;

  public MutationStrategies(final MutationEngineFactory factory,final HistoryStore history,
      final CoverageGenerator coverage, final ListenerFactory listenerFactory) {
    this(factory, history, coverage,
        listenerFactory, new DefaultBuildVerifier());
  }

  private MutationStrategies(final MutationEngineFactory factory,
      final HistoryStore history, final CoverageGenerator coverage,
      final ListenerFactory listenerFactory, final BuildVerifier buildVerifier) {
    this.history = history;
    this.coverage = coverage;
    this.listenerFactory = listenerFactory;
    this.buildVerifier = buildVerifier;
    this.factory = factory;
  }

  public HistoryStore history() {
    return this.history;
  }

  public CoverageGenerator coverage() {
    return this.coverage;
  }

  public ListenerFactory listenerFactory() {
    return this.listenerFactory;
  }

  public BuildVerifier buildVerifier() {
    return this.buildVerifier;
  }

  public MutationEngineFactory factory() {
    return this.factory;
  }

  public MutationStrategies with(final MutationEngineFactory factory) {
    return new MutationStrategies(factory, this.history, this.coverage,
        this.listenerFactory, this.buildVerifier);
  }

  public MutationStrategies with(final BuildVerifier verifier) {
    return new MutationStrategies(this.factory, this.history, this.coverage,
        this.listenerFactory, verifier);
  }

}
