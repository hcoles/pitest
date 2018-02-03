package org.pitest.mutationtest.build.intercept.annotations;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import org.objectweb.asm.tree.AnnotationNode;
import org.pitest.bytecode.analysis.AnalysisFunctions;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
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
    this.skipClass = clazz.annotations().contains(avoidedAnnotation());
    if (!this.skipClass) {
      final FunctionalList<Predicate<MutationDetails>> methods = clazz.methods()
          .filter(hasAvoidedAnnotation())
          .map(AnalysisFunctions.matchMutationsInMethod());
      this.annotatedMethodMatcher = Prelude.or(methods);
    }
  }

  private Predicate<MethodTree> hasAvoidedAnnotation() {
    return a -> a.annotations().contains(avoidedAnnotation());
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
