package org.pitest.mutationtest.build;

import java.util.Collection;
import java.util.List;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.plugin.FeatureSelector;
import org.pitest.plugin.FeatureSetting;

public class CompoundInterceptorFactory {

  private final FeatureSelector<MutationInterceptorFactory> features;
  
  public CompoundInterceptorFactory(List<FeatureSetting> features,
      Collection<MutationInterceptorFactory> filters) {
    this.features = new FeatureSelector<MutationInterceptorFactory>(features, filters);
  }

  public MutationInterceptor createInterceptor(
      ReportOptions data,
      ClassByteArraySource source) {    
    List<MutationInterceptor> interceptors = FCollection.map(this.features.getActiveFeatures(),
        toInterceptor(features, data, source));
    return new CompoundMutationInterceptor(interceptors);
  }

  
  private static F<MutationInterceptorFactory, MutationInterceptor> toInterceptor(
      final FeatureSelector<MutationInterceptorFactory> features, final ReportOptions data, final ClassByteArraySource source) {  
        
    return new F<MutationInterceptorFactory, MutationInterceptor>() {
      @Override
      public MutationInterceptor apply(MutationInterceptorFactory a) {
        return a.createInterceptor(new InterceptorParameters(features.getSettingForFeature(a.provides().name()), data, source));
      }
    };
        
  }
 }