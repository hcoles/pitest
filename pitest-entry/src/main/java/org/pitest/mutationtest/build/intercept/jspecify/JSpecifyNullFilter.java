package org.pitest.mutationtest.build.intercept.jspecify;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.objectweb.asm.TypeReference;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.InnerClassNode;
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
 * the class and any enclosing classes, and finally the package (package-info)
 * decide. A declaration carrying both annotations is treated as having neither.
 * Only annotations from {@code org.jspecify.annotations} are recognised.
 */
public class JSpecifyNullFilter implements MutationInterceptor {

  private enum Nullness {
    NULL_MARKED, NULL_UNMARKED, UNSPECIFIED;

    Nullness orElse(Supplier<Nullness> fallback) {
      return this == UNSPECIFIED ? fallback.get() : this;
    }
  }

  private final ClassByteArraySource code;
  private final Map<String, Nullness> packageNullness = new HashMap<>();
  private final Map<String, Nullness> classNullnessCache = new HashMap<>();

  private ClassTree currentClass;
  private Nullness classNullness = Nullness.UNSPECIFIED;

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
    this.classNullness = classNullness(clazz);
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
    this.classNullness = Nullness.UNSPECIFIED;
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
    return nullness(method.annotations())
        .orElse(() -> this.classNullness) == Nullness.NULL_MARKED;
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

  private Nullness classNullness(ClassTree clazz) {
    final String key = clazz.rawNode().name;
    final Nullness cached = this.classNullnessCache.get(key);
    if (cached != null) {
      return cached;
    }
    final Nullness result = nullness(clazz.annotations())
        .orElse(() -> enclosingNullness(clazz));
    this.classNullnessCache.put(key, result);
    return result;
  }

  private Nullness enclosingNullness(ClassTree clazz) {
    final String outer = enclosingClassName(clazz);
    if (outer == null) {
      return packageNullness(clazz.name());
    }
    final Nullness cached = this.classNullnessCache.get(outer);
    if (cached != null) {
      return cached;
    }
    return this.code.getBytes(outer.replace('/', '.'))
        .map(ClassTree::fromBytes)
        .map(this::classNullness)
        .orElseGet(() -> packageNullness(clazz.name()));
  }

  private static String enclosingClassName(ClassTree clazz) {
    final String name = clazz.rawNode().name;
    String outer = clazz.rawNode().outerClass;
    for (final InnerClassNode each : clazz.rawNode().innerClasses) {
      if (each.name.equals(name) && each.outerName != null) {
        outer = each.outerName;
      }
    }
    return outer;
  }

  private Nullness packageNullness(ClassName clazz) {
    final String packageName = clazz.getPackage().asJavaName();
    return this.packageNullness.computeIfAbsent(packageName, this::readPackageNullness);
  }

  private Nullness readPackageNullness(String packageName) {
    final String packageInfo = packageName.isEmpty() ? "package-info"
        : packageName + ".package-info";
    return this.code.getBytes(packageInfo)
        .map(ClassTree::fromBytes)
        .map(t -> nullness(t.annotations()))
        .orElse(Nullness.UNSPECIFIED);
  }

  // both annotations on one declaration cancel out, as if neither were present
  private static Nullness nullness(List<AnnotationNode> annotations) {
    final boolean marked = annotations.stream().anyMatch(a -> isNamed(a, "NullMarked"));
    final boolean unmarked = annotations.stream().anyMatch(a -> isNamed(a, "NullUnmarked"));
    if (marked == unmarked) {
      return Nullness.UNSPECIFIED;
    }
    return marked ? Nullness.NULL_MARKED : Nullness.NULL_UNMARKED;
  }

  private static boolean isNamed(AnnotationNode a, String simpleName) {
    return a.desc.equals("Lorg/jspecify/annotations/" + simpleName + ";");
  }


}
