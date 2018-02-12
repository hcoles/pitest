package org.pitest.mutationtest.build.intercept.annotations;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.objectweb.asm.tree.AnnotationNode;
import org.pitest.bytecode.analysis.AnalysisFunctions;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

public class ExcludedAnnotationInterceptor implements MutationInterceptor {

  private final List<String> configuredAnnotations;

  private boolean skipClass;
  private Predicate<MutationDetails> annotatedMethodMatcher;


  ExcludedAnnotationInterceptor(List<String> configuredAnnotations) {
    this.configuredAnnotations = configuredAnnotations;
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    this.skipClass = clazz.annotations().stream()
        .filter(avoidedAnnotation())
        .findFirst().isPresent();
    if (!this.skipClass) {
      final List<Predicate<MutationDetails>> methods = clazz.methods().stream()
          .filter(hasAvoidedAnnotation())
          .map(AnalysisFunctions.matchMutationsInMethod())
          .collect(Collectors.toList());
      this.annotatedMethodMatcher = Prelude.or(methods);
    }
  }

  private Predicate<MethodTree> hasAvoidedAnnotation() {
    return a -> a.annotations().stream()
        .filter(avoidedAnnotation())
        .findFirst().isPresent();
  }

  private Predicate<AnnotationNode> avoidedAnnotation() {
    return a -> shouldAvoid(a.desc);
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    if (this.skipClass) {
      return Collections.emptyList();
    }

    return FCollection.filter(mutations, Prelude.not(this.annotatedMethodMatcher));
  }

  @Override
  public void end() {

  }

  boolean shouldAvoid(String desc) {
    final String matchAgainst = desc.replace(";", "");
    for (final String each : this.configuredAnnotations) {
      if (matchAgainst.endsWith(each)) {
        return true;
      }
    }
    return false;
  }

}
