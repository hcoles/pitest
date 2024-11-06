package org.pitest.mutationtest.build.intercept.annotations;

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

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        .anyMatch(avoidedAnnotation());
    if (!this.skipClass) {
      // 1. Collect methods with avoided annotations
      final List<MethodTree> avoidedMethods = clazz.methods().stream()
          .filter(hasAvoidedAnnotation())
          .collect(Collectors.toList());

      final Set<String> avoidedMethodNames = avoidedMethods.stream()
          .map(method -> method.rawNode().name)
          .collect(Collectors.toSet());

      // 2. Collect lambda methods with being inside avoided methods
      final List<MethodTree> lambdaMethods = clazz.methods().stream()
          .filter(MethodTree::isGeneratedLambdaMethod)
          .filter(lambdaMethod -> {
            String lambdaName = lambdaMethod.rawNode().name; // e.g., lambda$fooWithLambdas$0
            String enclosingMethodName = extractEnclosingMethodName(lambdaName);

            return avoidedMethodNames.contains(enclosingMethodName);
          })
          .collect(Collectors.toList());

      // 3. Merge the two lists into a single list and cast MethodTree to Predicate<MutationDetails>
      final List<Predicate<MutationDetails>> mutationPredicates = Stream.concat(avoidedMethods.stream(), lambdaMethods.stream())
          .map(AnalysisFunctions.matchMutationsInMethod())
          .collect(Collectors.toList());

      this.annotatedMethodMatcher = Prelude.or(mutationPredicates);
    }
  }

  /**
   * TODO: maybe move to MethodTree class?? WDYT?
   * Extracts the enclosing method name from a lambda method's name.
   * Assumes lambda methods follow the naming convention: lambda$enclosingMethodName$number
   *
   * @param lambdaName The name of the lambda method (e.g., "lambda$fooWithLambdas$0")
   * @return The name of the enclosing method (e.g., "fooWithLambdas")
   */
  private String extractEnclosingMethodName(String lambdaName) {
    int firstDollar = lambdaName.indexOf('$');
    int secondDollar = lambdaName.indexOf('$', firstDollar + 1);

    if (firstDollar != -1 && secondDollar != -1) {
      return lambdaName.substring(firstDollar + 1, secondDollar);
    }
    return lambdaName;
  }

  private Predicate<MethodTree> hasAvoidedAnnotation() {
    return methodTree ->
        methodTree.annotations().stream().anyMatch(avoidedAnnotation());
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
