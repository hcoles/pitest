package org.pitest.mutationtest.build.intercept.annotations;

import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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
          .filter(hasAvoidedAnnotation())
          .collect(Collectors.toList());

      // Collect method names along with descriptors to handle overloaded methods
      final Set<MethodSignature> avoidedMethodSignatures = avoidedMethods.stream()
          .map(method -> new MethodSignature(method.rawNode().name, method.rawNode().desc))
          .collect(Collectors.toSet());

      // Keep track of processed methods to avoid infinite loops
      Set<MethodSignature> processedMethods = new HashSet<>(avoidedMethodSignatures);

      // 2. For each avoided method, collect lambda methods recursively
      for (MethodTree avoidedMethod : avoidedMethods) {
        collectLambdaMethods(avoidedMethod, clazz, avoidedMethodSignatures, processedMethods);
      }

      // 3. Create a predicate to match mutations in methods to avoid
      this.annotatedMethodMatcher = mutation -> {
        MethodSignature mutationSignature = new MethodSignature(
            mutation.getMethod(), mutation.getId().getLocation().getMethodDesc());
        return avoidedMethodSignatures.contains(mutationSignature);
      };
    }
  }

  /**
   * Recursively collects lambda methods defined within the given method.
   *
   * @param method The method to inspect for lambdas.
   * @param clazz The class containing the methods.
   * @param avoidedMethodSignatures The set of method signatures to avoid.
   * @param processedMethods The set of already processed methods to prevent infinite loops.
   */
  private void collectLambdaMethods(MethodTree method, ClassTree clazz,
                                    Set<MethodSignature> avoidedMethodSignatures,
                                    Set<MethodSignature> processedMethods) {
    Queue<MethodTree> methodsToProcess = new LinkedList<>();
    methodsToProcess.add(method);

    while (!methodsToProcess.isEmpty()) {
      MethodTree currentMethod = methodsToProcess.poll();

      for (AbstractInsnNode insn : currentMethod.rawNode().instructions) {
        if (insn instanceof InvokeDynamicInsnNode) {
          InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) insn;

          for (Object bsmArg : indy.bsmArgs) {
            if (bsmArg instanceof Handle) {
              Handle handle = (Handle) bsmArg;
              // Check if the method is in the same class and is a lambda method
              if (handle.getOwner().equals(clazz.rawNode().name) && handle.getName().startsWith("lambda$")) {
                MethodSignature lambdaMethodSignature = new MethodSignature(handle.getName(), handle.getDesc());
                if (!avoidedMethodSignatures.contains(lambdaMethodSignature)
                    && !processedMethods.contains(lambdaMethodSignature)) {
                  avoidedMethodSignatures.add(lambdaMethodSignature);
                  processedMethods.add(lambdaMethodSignature);
                  // Find the MethodTree for this lambda method
                  MethodTree lambdaMethod = findMethodTree(clazz, handle.getName(), handle.getDesc());
                  if (lambdaMethod != null) {
                    methodsToProcess.add(lambdaMethod);
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  private MethodTree findMethodTree(ClassTree clazz, String methodName, String methodDesc) {
    return clazz.methods().stream()
        .filter(m -> m.rawNode().name.equals(methodName) && m.rawNode().desc.equals(methodDesc))
        .findFirst()
        .orElse(null);
  }

  /**
   * Creates a predicate that checks if a method has an avoided annotation.
   *
   * @return A predicate that returns true if the method should be avoided.
   */
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

  /**
   * Represents a method signature with its name and descriptor.
   * Used to uniquely identify methods, especially overloaded ones.
   */
  private static class MethodSignature {
    private final String name;
    private final String desc;

    MethodSignature(String name, String desc) {
      this.name = name;
      this.desc = desc;
    }

    @Override
    public boolean equals(Object obj) {
      if (this == obj) {
        return true;
      }
      if (obj == null || getClass() != obj.getClass()) {
        return false;
      }
      MethodSignature that = (MethodSignature) obj;
      return name.equals(that.name) && desc.equals(that.desc);
    }

    @Override
    public int hashCode() {
      return name.hashCode() * 31 + desc.hashCode();
    }
  }
}
