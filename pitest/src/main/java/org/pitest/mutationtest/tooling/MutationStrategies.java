package org.pitest.mutationtest.tooling;

import org.pitest.coverage.CoverageGenerator;
import org.pitest.mutationtest.HistoryStore;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.verify.BuildVerifier;
import org.pitest.mutationtest.verify.DefaultBuildVerifier;
import org.pitest.util.ResultOutputStrategy;

public class MutationStrategies {

  private final HistoryStore                  history;
  private final CoverageGenerator             coverage;
  private final MutationResultListenerFactory listenerFactory;
  private final BuildVerifier                 buildVerifier;
  private final MutationEngineFactory         factory;
  private final ResultOutputStrategy          output;

  public MutationStrategies(final MutationEngineFactory factory,
      final HistoryStore history, final CoverageGenerator coverage,
      final MutationResultListenerFactory listenerFactory,
      final ResultOutputStrategy output) {
    this(factory, history, coverage, listenerFactory, output,
        new DefaultBuildVerifier());
  }

  private MutationStrategies(final MutationEngineFactory factory,
      final HistoryStore history, final CoverageGenerator coverage,
      final MutationResultListenerFactory listenerFactory,
      final ResultOutputStrategy output, final BuildVerifier buildVerifier) {
    this.history = history;
    this.coverage = coverage;
    this.listenerFactory = listenerFactory;
    this.buildVerifier = buildVerifier;
    this.factory = factory;
    this.output = output;
  }

  public HistoryStore history() {
    return this.history;
  }

  public CoverageGenerator coverage() {
    return this.coverage;
  }

  public MutationResultListenerFactory listenerFactory() {
    return this.listenerFactory;
  }

  public BuildVerifier buildVerifier() {
    return this.buildVerifier;
  }

  public MutationEngineFactory factory() {
    return this.factory;
  }

  public ResultOutputStrategy output() {
    return this.output;
  }

  public MutationStrategies with(final MutationEngineFactory factory) {
    return new MutationStrategies(factory, this.history, this.coverage,
        this.listenerFactory, this.output, this.buildVerifier);
  }

  public MutationStrategies with(final BuildVerifier verifier) {
    return new MutationStrategies(this.factory, this.history, this.coverage,
        this.listenerFactory, this.output, verifier);
  }

}
