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

  public MutationStrategies(MutationEngineFactory factory, HistoryStore history,
                            CoverageGenerator coverage,
                            MutationResultListenerFactory listenerFactory,
                            ResultOutputStrategy output) {
    this(factory, history, coverage, listenerFactory, output,
         new DefaultBuildVerifier());
  }

  private MutationStrategies(MutationEngineFactory factory,
                             HistoryStore history, CoverageGenerator coverage,
                             MutationResultListenerFactory listenerFactory,
                             ResultOutputStrategy output,
                             BuildVerifier buildVerifier) {
    this.history = history;
    this.coverage = coverage;
    this.listenerFactory = listenerFactory;
    this.buildVerifier = buildVerifier;
    this.factory = factory;
    this.output = output;
  }

  public HistoryStore history() {
    return history;
  }

  public CoverageGenerator coverage() {
    return coverage;
  }

  public MutationResultListenerFactory listenerFactory() {
    return listenerFactory;
  }

  public BuildVerifier buildVerifier() {
    return buildVerifier;
  }

  public MutationEngineFactory factory() {
    return factory;
  }

  public ResultOutputStrategy output() {
    return output;
  }

  public MutationStrategies with(MutationEngineFactory factory) {
    return new MutationStrategies(factory, history, coverage, listenerFactory,
                                  output, buildVerifier);
  }

  public MutationStrategies with(BuildVerifier verifier) {
    return new MutationStrategies(factory, history, coverage, listenerFactory,
                                  output, verifier);
  }
}
