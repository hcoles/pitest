package org.pitest.mutationtest.engine.gregor.mutators.returns;

import java.util.Collections;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.TypePath;
import org.objectweb.asm.TypeReference;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

/**
 * Mutates object return values to always return null
 *
 * Does not mutate return types for which a more stable return value
 * mutation exists.
 *
 * Does not mutate methods annotated with NotNull, or methods in a class or
 * package declared {@code @NullMarked} (JSpecify) whose return type is not
 * marked {@code @Nullable}. Under {@code @NullMarked} nullness checking rejects
 * a null return before the class is built, so the mutant could never be killed.
 */
public enum NullReturnValsMutator implements MethodMutatorFactory {

  NULL_RETURNS;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {

    if (!moreStableMutationExits(methodInfo)) {
      return new NullReturnMethodVisitor(this, methodInfo, context,
          methodVisitor);
    } else {
      return methodVisitor;
    }
  }

  private boolean moreStableMutationExits(MethodInfo methodInfo) {
    final Type type = Type.getReturnType(methodInfo.getMethodDescriptor());
    return type.getClassName().equals("java.lang.Boolean")
        || AReturnMethodVisitor.NON_NULL_MUTATIONS.containsKey(type.getClassName());
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String getName() {
    return name();
  }

}

class NullReturnMethodVisitor extends AbstractInsnMutator {

  private final MethodInfo methodInfo;

  private boolean hasNotNullAnnotation;
  private boolean hasNullableAnnotation;
  private Boolean methodNullMarked;


  NullReturnMethodVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
    this.methodInfo = methodInfo;
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    this.hasNotNullAnnotation |= desc.endsWith("NotNull;");
    this.hasNullableAnnotation |= desc.endsWith("Nullable;");
    if (desc.endsWith("NullMarked;")) {
      this.methodNullMarked = Boolean.TRUE;
    } else if (desc.endsWith("NullUnmarked;")) {
      this.methodNullMarked = Boolean.FALSE;
    }
    return super.visitAnnotation(desc, visible);
  }

  @Override
  public AnnotationVisitor visitTypeAnnotation(int typeRef, TypePath typePath,
      String desc, boolean visible) {
    // @Nullable on the return type arrives as a type annotation, not as a
    // method annotation.
    if ((typeRef >>> 24) == TypeReference.METHOD_RETURN
        && desc.endsWith("Nullable;")) {
      this.hasNullableAnnotation = true;
    }
    return super.visitTypeAnnotation(typeRef, typePath, desc, visible);
  }

  @Override
  protected boolean canMutate(final int opcode) {
    return super.canMutate(opcode) && !nullIsImpossible();
  }

  /**
   * Nullness defaults, most specific first: the method itself, then its class
   * (which inherited the package default when it was registered).
   */
  private boolean nullIsImpossible() {
    if (this.hasNotNullAnnotation) {
      return true;
    }
    if (this.hasNullableAnnotation) {
      return false;
    }
    final Boolean nullMarked = this.methodNullMarked != null
        ? this.methodNullMarked
        : this.methodInfo.getOwningClass().getNullMarked();
    return Boolean.TRUE.equals(nullMarked);
  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return Collections.singletonMap(Opcodes.ARETURN, nullReturn());
  }

  private static ZeroOperandMutation nullReturn() {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String describe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return value with null for " + methodInfo.getDescription();
      }

    };
  }

}
