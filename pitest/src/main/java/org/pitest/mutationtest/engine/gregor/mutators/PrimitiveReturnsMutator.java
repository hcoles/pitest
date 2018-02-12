package org.pitest.mutationtest.engine.gregor.mutators;

import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LRETURN;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

/**
 * Replaces primitive return values with 0. Does not mutate boolean
 * returns as these are handled by the BooleanFalseReturn mutator
 */
public enum PrimitiveReturnsMutator implements MethodMutatorFactory {

  PRIMITIVE_RETURN_VALS_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {

    if (!returnsBoolean(methodInfo)) {
      return new PrimitivesReturnValsMethodVisitor(this, methodInfo, context,
          methodVisitor);
    } else {
      return methodVisitor;
    }

  }

  private boolean returnsBoolean(MethodInfo methodInfo) {
    final int sort = Type.getReturnType(methodInfo.getMethodDescriptor()).getSort();
    return sort == Type.BOOLEAN;
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

class PrimitivesReturnValsMethodVisitor extends AbstractInsnMutator {

  PrimitivesReturnValsMethodVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

  static {
    MUTATIONS.put(IRETURN, ireturnMutation());
    MUTATIONS.put(DRETURN, dreturnMutation());
    MUTATIONS.put(FRETURN, freturnMutation());
    MUTATIONS.put(LRETURN, lreturnMutation());
  }

  private static ZeroOperandMutation lreturnMutation() {
    return new ZeroOperandMutation() {

      @Override
      public void apply(final int opcode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP2);
        mv.visitInsn(Opcodes.LCONST_0);
        mv.visitInsn(Opcodes.LRETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced long return with 0 for " + methodInfo.getDescription();
      }

    };
  }

  private static ZeroOperandMutation freturnMutation() {
    return new ZeroOperandMutation() {

      @Override
      public void apply(final int opcode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitInsn(Opcodes.FRETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced float return with 0.0f for " + methodInfo.getDescription();
      }

    };
  }

  private static ZeroOperandMutation dreturnMutation() {
    return new ZeroOperandMutation() {

      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP2);
        mv.visitInsn(Opcodes.DCONST_0);
        mv.visitInsn(Opcodes.DRETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced double return with 0.0d for " + methodInfo.getDescription();
      }

    };
  }


  private static ZeroOperandMutation ireturnMutation() {
    return new ZeroOperandMutation() {

      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IRETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return makeMessage(methodInfo.getMethodDescriptor()) + " for " + methodInfo.getDescription();
      }

      private String makeMessage(String methodDescriptor) {
        final int sort = Type.getReturnType(methodDescriptor).getSort();
        switch (sort) {
        case Type.BYTE:
          return "replaced byte return with 0";
        case Type.INT:
          return "replaced int return with 0";
        case Type.CHAR:
          return "replaced char return with 0";
        case Type.SHORT:
          return "replaced short return with 0";
        default:
          throw new IllegalStateException(
              methodDescriptor + " does not return integer type");
        }
      }

    };
  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return MUTATIONS;
  }
}

