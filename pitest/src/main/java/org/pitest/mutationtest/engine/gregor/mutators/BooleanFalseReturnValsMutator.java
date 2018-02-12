package org.pitest.mutationtest.engine.gregor.mutators;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.IRETURN;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

/**
 * Replaces primitive and boxed boolean return values with false
 */
public enum BooleanFalseReturnValsMutator implements MethodMutatorFactory {

  BOOLEAN_FALSE_RETURN;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {

    final Type returnType = Type.getReturnType(methodInfo.getMethodDescriptor());
    if ((returnType.getSort() == Type.BOOLEAN)
     || returnType.getClassName().equals("java.lang.Boolean")) {
      return new BooleanFalseMethodVisitor(this, methodInfo, context,
          methodVisitor);
    }

    return methodVisitor;
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

class BooleanFalseMethodVisitor extends AbstractInsnMutator {
  private static final String BOOLEAN = ClassName.fromClass(Boolean.class).asInternalName();

  BooleanFalseMethodVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

  static {
    MUTATIONS.put(IRETURN, ireturnMutation());
    MUTATIONS.put(ARETURN, areturnMutation());
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
        return "replaced boolean return with false for " + methodInfo.getDescription();
      }
    };
  }

  private static ZeroOperandMutation areturnMutation() {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, BOOLEAN, "valueOf", "(Z)Ljava/lang/Boolean;", false);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced Boolean return with False for " + methodInfo.getDescription();
      }
    };
  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return MUTATIONS;
  }
}