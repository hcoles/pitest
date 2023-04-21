package org.pitest.mutationtest.build;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.coverage.CoverageDatabase;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSelector;
import org.pitest.plugin.FeatureSetting;

import java.util.Collection;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CompoundInterceptorFactory {

  private final FeatureSelector<MutationInterceptorFactory> features;

  public CompoundInterceptorFactory(List<FeatureSetting> features,
      Collection<MutationInterceptorFactory> filters) {
    this.features = new FeatureSelector<>(features, filters);
  }

  public CompoundMutationInterceptor createInterceptor(
      ReportOptions data,
      CoverageDatabase coverage,
      ClassByteArraySource source,
      TestPrioritiser testPrioritiser
      ) {
    final List<MutationInterceptor> interceptors = this.features.getActiveFeatures().stream()
            .map(toInterceptor(this.features, data, coverage, source, testPrioritiser))
            .collect(Collectors.toList());
    return new CompoundMutationInterceptor(interceptors);
  }


  private static Function<MutationInterceptorFactory, MutationInterceptor> toInterceptor(
          FeatureSelector<MutationInterceptorFactory> features,
          ReportOptions data,
          CoverageDatabase coverage,
          ClassByteArraySource source,
          TestPrioritiser testPrioritiser
          ) {

    return a -> a.createInterceptor(new InterceptorParameters(features.getSettingForFeature(a.provides().name()), data, coverage, source, testPrioritiser));

  }
 }