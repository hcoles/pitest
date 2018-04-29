package org.pitest.mutationtest.build.intercept.annotations;

import java.util.Arrays;
import java.util.List;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.FeatureParameter;

public class ExcludedAnnotationInterceptorFactory implements MutationInterceptorFactory  {

  private static final FeatureParameter ARGUMENT = FeatureParameter.named("annotation")
      .withDescription("Annotation to avoid (full package name not required)");

  @Override
  public String description() {
    return "Excluded annotations plugin";
  }

  @Override
  public MutationInterceptor createInterceptor(InterceptorParameters params) {
    return new ExcludedAnnotationInterceptor(determineAnnotations(params));
  }

  private List<String> determineAnnotations(InterceptorParameters params) {
    if (params.getList(ARGUMENT).isEmpty()) {
      return Arrays.asList("Generated", "DoNotMutate", "CoverageIgnore");
    }
    return params.getList(ARGUMENT);
  }

  @Override
  public Feature provides() {
    return Feature.named("FANN")
        .withOnByDefault(true)
        .withDescription("Filters mutations in classes and methods with matching annotations of class or runtime retention")
        .withParameter(ARGUMENT);
  }

}
