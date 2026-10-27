package org.pitest.mutationtest.build.intercept.jspecify;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.TypeAnnotationNode;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassName;

import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import static org.pitest.mutationtest.engine.gregor.mutators.returns.NullReturnValsMutator.NULL_RETURNS;

/**
 * Removes null return mutants from code that JSpecify declares non-null.
 * Nullness is resolved most specific first: @Nullable on the method or its
 * return type keeps the mutant, then @NullMarked / @NullUnmarked on the method,
 * the class, and finally the package (package-info) decide.
 * Only annotations from {@code org.jspecify.annotations} are recognised.
 */
public class JSpecifyNullFilter implements MutationInterceptor {

  private final ClassByteArraySource code;
  private final Map<String, Optional<Boolean>> packageNullness = new HashMap<>();

  private ClassTree currentClass;
  private Optional<Boolean> classNullMarked = Optional.empty();

  JSpecifyNullFilter(ClassByteArraySource code) {
    this.code = code;
  }

  @Override
  public InterceptorType type() {
    return InterceptorType.FILTER;
  }

  @Override
  public void begin(ClassTree clazz) {
    this.currentClass = clazz;
    this.classNullMarked = firstPresent(nullness(clazz.annotations()),
        () -> packageNullness(clazz.name()));
  }

  @Override
  public Collection<MutationDetails> intercept(
      Collection<MutationDetails> mutations, Mutater m) {
    return mutations.stream()
        .filter(md -> !isNullReturnInNonNullMethod(md))
        .collect(Collectors.toList());
  }

  @Override
  public void end() {
    this.currentClass = null;
    this.classNullMarked = Optional.empty();
  }

  private boolean isNullReturnInNonNullMethod(MutationDetails md) {
    if (!NULL_RETURNS.isMutatorFor(md.getId())) {
      return false;
    }
    return this.currentClass.method(md.getId().getLocation())
        .map(this::returnsNonNull)
        .orElse(false);
  }

  private boolean returnsNonNull(MethodTree method) {
    if (hasNullableReturn(method)) {
      return false;
    }
    return firstPresent(nullness(method.annotations()), () -> this.classNullMarked)
        .orElse(false);
  }

  private static boolean hasNullableReturn(MethodTree method) {
    if (method.annotations().stream().anyMatch(a -> isNamed(a, "Nullable"))) {
      return true;
    }
    return typeAnnotations(method).stream()
        .anyMatch(a -> isNamed(a, "Nullable")
            && new TypeReference(a.typeRef).getSort() == TypeReference.METHOD_RETURN
            && a.typePath == null);
  }

  private static List<TypeAnnotationNode> typeAnnotations(MethodTree method) {
    final List<TypeAnnotationNode> all = new java.util.ArrayList<>();
    if (method.rawNode().visibleTypeAnnotations != null) {
      all.addAll(method.rawNode().visibleTypeAnnotations);
    }
    if (method.rawNode().invisibleTypeAnnotations != null) {
      all.addAll(method.rawNode().invisibleTypeAnnotations);
    }
    return all;
  }

  private Optional<Boolean> packageNullness(ClassName clazz) {
    final String packageName = clazz.getPackage().asJavaName();
    return this.packageNullness.computeIfAbsent(packageName, this::readPackageNullness);
  }

  private Optional<Boolean> readPackageNullness(String packageName) {
    final String packageInfo = packageName.isEmpty() ? "package-info"
        : packageName + ".package-info";
    return this.code.getBytes(packageInfo)
        .map(ClassTree::fromBytes)
        .flatMap(t -> nullness(t.annotations()));
  }

  private static Optional<Boolean> firstPresent(Optional<Boolean> first,
      Supplier<Optional<Boolean>> fallback) {
    return first.isPresent() ? first : fallback.get();
  }

  private static Optional<Boolean> nullness(List<AnnotationNode> annotations) {
    for (final AnnotationNode each : annotations) {
      if (isNamed(each, "NullMarked")) {
        return Optional.of(true);
      }
      if (isNamed(each, "NullUnmarked")) {
        return Optional.of(false);
      }
    }
    return Optional.empty();
  }

  private static boolean isNamed(AnnotationNode a, String simpleName) {
    return a.desc.equals("Lorg/jspecify/annotations/" + simpleName + ";");
  }

}
