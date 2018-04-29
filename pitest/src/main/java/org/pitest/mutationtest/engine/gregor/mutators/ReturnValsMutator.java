/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.engine.gregor.mutators;

import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.FRETURN;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LCONST_1;
import static org.objectweb.asm.Opcodes.LRETURN;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

public enum ReturnValsMutator implements MethodMutatorFactory {

  RETURN_VALS_MUTATOR;

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new ReturnValsMethodVisitor(this, methodInfo, context, methodVisitor);
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

class ReturnValsMethodVisitor extends AbstractInsnMutator {

  ReturnValsMethodVisitor(final MethodMutatorFactory factory,
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
    MUTATIONS.put(ARETURN, areturnMutation());
  }

  private static ZeroOperandMutation areturnMutation() {
    return new ZeroOperandMutation() {

      @Override
      public void apply(final int opCode, final MethodVisitor mv) {

        // Strategy translated from jumble BCEL code
        // if result is non-null make it null, otherwise hard case
        // for moment throw runtime exception
        final Label l1 = new Label();
        mv.visitJumpInsn(Opcodes.IFNONNULL, l1);
        mv.visitTypeInsn(Opcodes.NEW, "java/lang/RuntimeException");
        mv.visitInsn(Opcodes.DUP);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/RuntimeException",
            "<init>", "()V", false);
        mv.visitInsn(Opcodes.ATHROW);
        mv.visitLabel(l1);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitInsn(Opcodes.ARETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "mutated return of Object value for "
            + methodInfo.getDescription()
            + " to ( if (x != null) null else throw new RuntimeException )";
      }

    };
  }

  private static ZeroOperandMutation lreturnMutation() {
    return new ZeroOperandMutation() {

      @Override
      public void apply(final int opcode, final MethodVisitor mv) {
        mv.visitInsn(LCONST_1);
        mv.visitInsn(LADD);
        mv.visitInsn(opcode);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return of long value with value + 1 for "
            + methodInfo.getDescription();
      }

    };
  }

  private static ZeroOperandMutation freturnMutation() {
    return new ZeroOperandMutation() {

      @Override
      public void apply(final int opcode, final MethodVisitor mv) {
        // Strategy translated from jumble BCEL code
        // The following is complicated by the problem of NaNs. By default
        // the new value is -(x + 1), but this doesn't work for NaNs. But
        // for a NaN x != x is true, and we use this to detect them.
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.FCMPG);
        final Label l1 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, l1);
        mv.visitInsn(Opcodes.POP);
        mv.visitInsn(Opcodes.FCONST_0);
        mv.visitLabel(l1);
        mv.visitInsn(Opcodes.FCONST_1);
        mv.visitInsn(Opcodes.FADD);
        mv.visitInsn(Opcodes.FNEG);
        mv.visitInsn(Opcodes.FRETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return of float value with -(x + 1) for "
            + methodInfo.getDescription();
      }

    };
  }

  private static ZeroOperandMutation dreturnMutation() {
    return new ZeroOperandMutation() {

      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        // Strategy translated from jumble BCEL code
        // The following is complicated by the problem of NaNs. By default
        // the new value is -(x + 1), but this doesn't work for NaNs. But
        // for a NaN x != x is true, and we use this to detect them.
        mv.visitInsn(Opcodes.DUP2);
        mv.visitInsn(Opcodes.DUP2);
        mv.visitInsn(Opcodes.DCMPG);
        final Label l1 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, l1);
        mv.visitInsn(Opcodes.POP2);
        mv.visitInsn(Opcodes.DCONST_0);
        mv.visitLabel(l1);
        mv.visitInsn(Opcodes.DCONST_1);
        mv.visitInsn(Opcodes.DADD);
        mv.visitInsn(Opcodes.DNEG);
        mv.visitInsn(Opcodes.DRETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return of double value with -(x + 1) for "
            + methodInfo.getDescription();
      }

    };
  }

  private static ZeroOperandMutation ireturnMutation() {
    return new ZeroOperandMutation() {

      @Override
      public void apply(final int opCode, final MethodVisitor mv) {
        final Label l1 = new Label();
        mv.visitJumpInsn(Opcodes.IFEQ, l1);
        mv.visitInsn(Opcodes.ICONST_0);
        mv.visitInsn(Opcodes.IRETURN);
        mv.visitLabel(l1);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitInsn(Opcodes.IRETURN);
      }

      @Override
      public String decribe(final int opCode, final MethodInfo methodInfo) {
        return "replaced return of integer sized value with (x == 0 ? 1 : 0)";
      }

    };
  }

  @Override
  protected Map<Integer, ZeroOperandMutation> getMutations() {
    return MUTATIONS;
  }

}
