package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.Collections;
import java.util.Map;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
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
 * Does not mutate methods annotated with NotNull
 */
public enum NullReturnValsMutator implements MethodMutatorFactory {

  NULL_RETURN_VALUES;

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
        || AReturnMethodVisitor.NON_NULL_MUTATIONS.keySet().contains(type.getClassName());
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

  private boolean hasNotNullAnnotation;


  NullReturnMethodVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  @Override
  public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
    this.hasNotNullAnnotation |= desc.endsWith("NotNull;");
    return super.visitAnnotation(desc, visible);
  }

  @Override
  protected boolean canMutate(final int opcode) {
    return super.canMutate(opcode) && (!this.hasNotNullAnnotation);
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
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return value with null for " + methodInfo.getDescription();
      }

    };
  }

}