package org.pitest.mutationtest.build.intercept.annotations;

import java.util.Arrays;
import java.util.List;

import org.pitest.functional.Option;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureSetting;

public class ExcludedAnnotationInterceptorFactory implements MutationInterceptorFactory  {
  
  private static final String ARGUMENT = "annotation";

  @Override
  public String description() {
    return "Excluded annotations plugin";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new ExcludedAnnotationInterceptor(determineAnnotations(params.settings()));
  }

  private List<String> determineAnnotations(Option<FeatureSetting> settings) {
    if (settings.hasNone() || settings.value().getList(ARGUMENT).isEmpty()) {
      return Arrays.asList("Generated", "DoNotMutate", "CoverageIgnore");
    }
    return settings.value().getList(ARGUMENT); 
  }

  @Override
  public Feature provides() {
    return Feature.named("FANN")
        .withOnByDefault(true)
        .withDescription("Filters mutations in classes and methods with matching annotations of class or runtime retention");
  }

}
