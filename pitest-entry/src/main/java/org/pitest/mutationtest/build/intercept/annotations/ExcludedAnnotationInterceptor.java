package org.pitest.mutationtest.build.intercept.annotations;

import org.objectweb.asm.Handle;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InvokeDynamicInsnNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;
import java.util.List;
import java.util.Queue;
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

      // Collect method names along with descriptors to handle overloaded methods
      final Set<Location> avoidedMethodSignatures = avoidedMethods.stream()
              .map(method -> new Location(clazz.name(), method.rawNode().name, method.rawNode().desc))
              .collect(Collectors.toSet());

      // Keep track of processed methods to avoid infinite loops - TODO not clear
      // that this is necessary
      Set<Location> processedMethods = new HashSet<>(avoidedMethodSignatures);

      // 2. For each avoided method, collect lambda methods recursively
      for (MethodTree avoidedMethod : avoidedMethods) {
        collectLambdaMethods(avoidedMethod, clazz, avoidedMethodSignatures, processedMethods);
      }

      // 3. Create a predicate to match mutations in methods to avoid
      this.annotatedMethodMatcher = mutation -> {
        Location mutationSignature = Location.location(clazz.name(),
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
                                    Set<Location> avoidedMethodSignatures,
                                    Set<Location> processedMethods) {
    Queue<MethodTree> methodsToProcess = new LinkedList<>();
    methodsToProcess.add(method);

    while (!methodsToProcess.isEmpty()) {
      MethodTree currentMethod = methodsToProcess.poll();
      Set<Location> lambdas = currentMethod.instructions().stream()
              .flatMap(n -> lambdaCallsToClass(clazz.name(), n))
              .filter(l -> !avoidedMethodSignatures.contains(l) && !processedMethods.contains(l))
              .collect(Collectors.toSet());

      List<MethodTree> recurse = lambdas.stream()
              .map(clazz::method)
              .flatMap(Optional::stream)
              .collect(Collectors.toList());

      methodsToProcess.addAll(recurse);

      avoidedMethodSignatures.addAll(lambdas);
      processedMethods.addAll(lambdas);
    }
  }

  private Stream<Location> lambdaCallsToClass(ClassName clazz, AbstractInsnNode insn) {
    if (!(insn instanceof InvokeDynamicInsnNode)) {
      return Stream.empty();
    }

    InvokeDynamicInsnNode indy = (InvokeDynamicInsnNode) insn;

    for (Object bsmArg : indy.bsmArgs) {
      if (bsmArg instanceof Handle) {
        Handle handle = (Handle) bsmArg;
        // Check if the method is in the same class and is a lambda method
        if (handle.getOwner().equals(clazz.asInternalName()) && handle.getName().startsWith("lambda$")) {
          return Stream.of(Location.location(clazz,handle.getName(), handle.getDesc()));
        }
      }
    }
    return Stream.empty();
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

    return mutations.stream()
            .filter(this.annotatedMethodMatcher.negate())
            .collect(Collectors.toList());
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
