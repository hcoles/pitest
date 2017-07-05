package org.pitest.mutationtest.build.intercept.annotations;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;
import org.pitest.bytecode.analysis.AnalysisFunctions;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.predicate.Predicate;
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
    skipClass = clazz.annotations().contains(avoidedAnnotation());
    if (!skipClass) {
      FunctionalList<Predicate<MutationDetails>> methods = clazz.methods()
          .filter(hasAvoidedAnnotation())
          .map(AnalysisFunctions.matchMutationsInMethod());
      annotatedMethodMatcher = Prelude.or(methods);
    }
  }

  private F<MethodTree, Boolean> hasAvoidedAnnotation() {
    return new F<MethodTree, Boolean>() {
      @Override
      public Boolean apply(MethodTree a) {
        return a.annotations().contains(avoidedAnnotation());
      }
    };
  }

  private F<AnnotationNode, Boolean> avoidedAnnotation() {
    return new F<AnnotationNode, Boolean>() {
      @Override
      public Boolean apply(AnnotationNode a) {
        return shouldAvoid(a.desc);
      }
    };
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    if (skipClass) {
      return Collections.emptyList();
    }
    
    return FCollection.filter(mutations, Prelude.not(annotatedMethodMatcher));
  }

  @Override
  public void end() {
    
  }
  
  boolean shouldAvoid(String desc) {
    String matchAgainst = desc.replace(";", "");
    for (String each : configuredAnnotations) {
      if (matchAgainst.endsWith(each)) {
        return true;
      }
    }
    return false;
  }

}
