package org.pitest.mutationtest.build.intercept.annotations;

import org.objectweb.asm.tree.AnnotationNode;
import org.pitest.bytecode.analysis.AnalysisFunctions;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.functional.FCollection;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
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
      // 1. Collect methods with avoided annotations or that override such methods
      final List<MethodTree> avoidedMethods = clazz.methods().stream()
          .filter(hasAvoidedAnnotationOrOverridesMethodWithAvoidedAnnotation(clazz))
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

  /**
   * Creates a predicate that checks if a method has an avoided annotation or overrides a method
   * in its superclass hierarchy that has an avoided annotation.
   *
   * @param clazz The class tree of the current class.
   * @return A predicate that returns true if the method should be avoided.
   */
  private Predicate<MethodTree> hasAvoidedAnnotationOrOverridesMethodWithAvoidedAnnotation(ClassTree clazz) {
    return methodTree ->
        methodTree.annotations().stream().anyMatch(avoidedAnnotation())
            || isOverridingMethodWithAvoidedAnnotation(methodTree, clazz);
  }

  /**
   * Checks if the given method overrides a method in its superclass hierarchy that has an avoided annotation.
   *
   * @param method The method to check.
   * @param clazz  The class tree of the current class.
   * @return True if the method overrides an annotated method; false otherwise.
   */
  private boolean isOverridingMethodWithAvoidedAnnotation(MethodTree method, ClassTree clazz) {
    String methodName = method.rawNode().name;
    String methodDesc = method.rawNode().desc;
    return isMethodInSuperClassWithAvoidedAnnotation(methodName, methodDesc, clazz);
  }

  /**
   * Recursively checks if a method with the given name and descriptor exists in the superclass hierarchy
   * and has an avoided annotation.
   *
   * @param methodName The name of the method to search for.
   * @param methodDesc The descriptor of the method to search for.
   * @param clazz      The class tree of the current class or superclass.
   * @return True if an annotated method is found in the superclass hierarchy; false otherwise.
   */
  private boolean isMethodInSuperClassWithAvoidedAnnotation(String methodName, String methodDesc, ClassTree clazz) {
    String superClassName = clazz.rawNode().superName;
    if (superClassName == null || superClassName.equals("java/lang/Object")) {
      return false;
    }

    ClassloaderByteArraySource source = ClassloaderByteArraySource.fromContext();
    Optional<byte[]> superClassBytes = source.getBytes(superClassName.replace('/', '.'));
    if (!superClassBytes.isPresent()) {
      return false;
    }

    ClassTree superClassTree = ClassTree.fromBytes(superClassBytes.get());

    Optional<MethodTree> superMethod = superClassTree.methods().stream()
        .filter(m -> m.rawNode().name.equals(methodName) && m.rawNode().desc.equals(methodDesc))
        .findFirst();

    if (superMethod.isPresent()) {
      if (superMethod.get().annotations().stream().anyMatch(avoidedAnnotation())) {
        return true;
      } else {
        // continue recursion to check superclass chain
        return isMethodInSuperClassWithAvoidedAnnotation(methodName, methodDesc, superClassTree);
      }
    } else {
      //  method not found in this superclass, continue searching up the hierarchy
      return false;
    }
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
