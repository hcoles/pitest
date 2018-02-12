package org.pitest.mutationtest.engine.gregor.mutators;

import java.util.Collections;
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
 * Mutates object return values to always return an "empty" value such
 * as 0, Optional.none etc.
 *
 * Does not mutate Booleans as this mutation is created by the BooleanFalseReturn
 * mutator.
 *
 */
public enum EmptyObjectReturnValsMutator implements MethodMutatorFactory {

  EMPTY_RETURN_VALUES;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {

    if (!returnsBoolean(methodInfo)) {
      return new AReturnMethodVisitor(this, methodInfo, context,
          methodVisitor);
    } else {
      return methodVisitor;
    }
  }

  private boolean returnsBoolean(MethodInfo methodInfo) {
    final Type type = Type.getReturnType(methodInfo.getMethodDescriptor());
    return type.getClassName().equals("java.lang.Boolean");
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

class AReturnMethodVisitor extends AbstractInsnMutator {

  static final Map<String, ZeroOperandMutation> NON_NULL_MUTATIONS = new HashMap<>();
  static {
    NON_NULL_MUTATIONS.put("java.lang.Integer", returnIntegerZero(Integer.class, "(I)Ljava/lang/Integer;", "replaced Integer return value with 0"));
    NON_NULL_MUTATIONS.put("java.lang.Short", returnIntegerZero(Short.class, "(S)Ljava/lang/Short;",  "replaced Short return value with 0"));
    NON_NULL_MUTATIONS.put("java.lang.Character", returnIntegerZero(Character.class, "(C)Ljava/lang/Character;",  "replaced Character return value with 0"));
    NON_NULL_MUTATIONS.put("java.lang.Long", returnLongZero());
    NON_NULL_MUTATIONS.put("java.lang.Float", returnFloatZero());
    NON_NULL_MUTATIONS.put("java.lang.Double", returnDoubleZero());
    NON_NULL_MUTATIONS.put("java.lang.String", returnEmptyString());
    NON_NULL_MUTATIONS.put("java.util.Optional", returnEmptyOptional());
    NON_NULL_MUTATIONS.put("java.util.List", returnEmptyList());
    NON_NULL_MUTATIONS.put("java.util.Set", returnEmptySet());
    NON_NULL_MUTATIONS.put("java.util.Collection", returnEmptyList());
  }

  AReturnMethodVisitor(final MethodMutatorFactory factory,
      final MethodInfo methodInfo, final MutationContext context,
      final MethodVisitor writer) {
    super(factory, methodInfo, context, writer);
  }

  @Override
  protected boolean canMutate(final int opcode) {
    return super.canMutate(opcode) && canMutateToNonNull();
  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return Collections.singletonMap(Opcodes.ARETURN, areturnMutation());
  }

  private ZeroOperandMutation areturnMutation() {
    return NON_NULL_MUTATIONS.get(currentReturnType());
  }

  private boolean canMutateToNonNull() {
    return NON_NULL_MUTATIONS.containsKey(currentReturnType());
  }

  private String currentReturnType() {
    final Type t = Type.getReturnType(methodInfo().getMethodDescriptor());
    return t.getClassName();
  }

  private static ZeroOperandMutation returnIntegerZero(final Class<?> owner, final String sig, final String msg) {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, ClassName.fromClass(owner).asInternalName(), "valueOf", sig, false);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return msg + " for " + methodInfo.getDescription();
      }
    };
  }

  private static ZeroOperandMutation returnLongZero() {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.LCONST_0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, ClassName.fromClass(Long.class).asInternalName(), "valueOf", "(J)Ljava/lang/Long;", false);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced Long return value with 0L for " + methodInfo.getDescription();
      }

    };
  }


  private static ZeroOperandMutation returnDoubleZero() {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.DCONST_0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, ClassName.fromClass(Double.class).asInternalName(), "valueOf", "(D)Ljava/lang/Double;", false);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced Double return value with 0 for " + methodInfo.getDescription();
      }
    };
  }

  private static ZeroOperandMutation returnFloatZero() {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, ClassName.fromClass(Float.class).asInternalName(), "valueOf", "(F)Ljava/lang/Float;", false);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced Float return value with 0 for " + methodInfo.getDescription();
      }
    };
  }

  private static ZeroOperandMutation returnEmptyString() {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitLdcInsn("");
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return value with \"\" for " + methodInfo.getDescription();
      }
    };
  }

  private static ZeroOperandMutation returnEmptyList() {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Collections", "emptyList", "()Ljava/util/List;", false);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return value with Collections.emptyList for " + methodInfo.getDescription();
      }
    };
  }

  private static ZeroOperandMutation returnEmptySet() {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Collections", "emptySet", "()Ljava/util/Set;", false);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return value with Collections.emptyList for " + methodInfo.getDescription();
      }
    };
  }

  private static ZeroOperandMutation returnEmptyOptional() {
    return new ZeroOperandMutation() {
      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        mv.visitInsn(Opcodes.POP);
        mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/util/Optional", "empty", "()Ljava/util/Optional;", false);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return value with Optional.empty for " + methodInfo.getDescription();
      }
    };
  }
}
