package org.pitest.mutationtest.tooling;

import org.pitest.coverage.CoverageGenerator;
import org.pitest.mutationtest.History;
import org.pitest.mutationtest.build.CoverageTransformer;
import org.pitest.mutationtest.MutationEngineFactory;
import org.pitest.mutationtest.MutationResultInterceptor;
import org.pitest.mutationtest.MutationResultListenerFactory;
import org.pitest.mutationtest.verify.BuildVerifier;
import org.pitest.util.ResultOutputStrategy;

public class MutationStrategies {

  private final History history;
  private final CoverageGenerator             coverage;
  private final MutationResultListenerFactory listenerFactory;
  private final MutationResultInterceptor     resultsInterceptor;

  private final CoverageTransformer           coverageTransformer;
  private final BuildVerifier                 buildVerifier;
  private final MutationEngineFactory         factory;
  private final ResultOutputStrategy          output;

  public MutationStrategies(final MutationEngineFactory factory,
                            final History history, final CoverageGenerator coverage,
                            final MutationResultListenerFactory listenerFactory,
                            final MutationResultInterceptor resultsInterceptor,
                            final CoverageTransformer coverageTransformer,
                            final ResultOutputStrategy output, final BuildVerifier buildVerifier) {
    this.history = history;
    this.coverage = coverage;
    this.listenerFactory = listenerFactory;
    this.resultsInterceptor = resultsInterceptor;
    this.coverageTransformer = coverageTransformer;
    this.buildVerifier = buildVerifier;
    this.factory = factory;
    this.output = output;
  }

  public History history() {
    return this.history;
  }

  public CoverageGenerator coverage() {
    return this.coverage;
  }

  public MutationResultListenerFactory listenerFactory() {
    return this.listenerFactory;
  }

  public MutationResultInterceptor resultInterceptor() {
    return this.resultsInterceptor;
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
        this.listenerFactory, this.resultsInterceptor, this.coverageTransformer, this.output, this.buildVerifier);
  }

  public MutationStrategies with(final BuildVerifier verifier) {
    return new MutationStrategies(this.factory, this.history, this.coverage,
        this.listenerFactory, this.resultsInterceptor, this.coverageTransformer, this.output, verifier);
  }

  public CoverageTransformer coverageTransformer() {
    return this.coverageTransformer;
  }
}
