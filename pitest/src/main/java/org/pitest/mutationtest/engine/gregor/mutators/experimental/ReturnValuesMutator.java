/*
 * Copyright 2011 Henry Coles and Stefan Penndorf
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
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 */
package org.pitest.mutationtest.engine.gregor.mutators.experimental;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.LineTrackingMethodAdapter;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

/**
 * The <code>ReturnValuesMutator</code> mutates the return values of method
 * calls. Depending on the return type of the method another mutation is used.
 * 
 * 
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class ReturnValuesMutator implements MethodMutatorFactory {

  private final class ReturnValuesMethodVisitor extends MethodAdapter {

    private static final String DESCRIPTION_MESSAGE_PATTERN = "replaced return of %s value with %s";

    private final Context       context;

    private ReturnValuesMethodVisitor(final Context context,
        final MethodVisitor delegateVisitor) {
      super(delegateVisitor);
      this.context = context;
    }

    private boolean shouldMutate(final String type, final String replacement) {

      final String description = String.format(DESCRIPTION_MESSAGE_PATTERN,
          type, replacement);

      final MutationIdentifier mutationId = this.context.registerMutation(
          ReturnValuesMutator.this, description);

      return this.context.shouldMutate(mutationId);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.MethodAdapter#visitInsn(int)
     */
    @Override
    public void visitInsn(final int opcode) {
      final Label label = new Label();

      switch (opcode) {
      case Opcodes.IRETURN:

        if (shouldMutate("primitive boolean/byte/short/integer",
            "(x == 1) ? 0 : x + 1")) {
          super.visitInsn(Opcodes.DUP);
          super.visitInsn(Opcodes.ICONST_1);
          super.visitJumpInsn(Opcodes.IF_ICMPEQ, label);

          super.visitInsn(Opcodes.ICONST_1);
          super.visitInsn(Opcodes.IADD);
          super.visitInsn(Opcodes.IRETURN);

          super.visitLabel(label);
          super.visitInsn(Opcodes.ICONST_0);
          super.visitInsn(Opcodes.IRETURN);
        }

        break;
      case Opcodes.LRETURN:

        if (shouldMutate("primitive long", "x + 1")) {
          super.visitInsn(Opcodes.LCONST_1);
          super.visitInsn(Opcodes.LADD);
          super.visitInsn(Opcodes.LRETURN);
        }

        break;
      case Opcodes.FRETURN:

        if (shouldMutate("primitive float", "-(x + 1)")) {
          // Strategy translated from jumble BCEL code
          // The following is complicated by the problem of NaNs. By default
          // the new value is -(x + 1), but this doesn't work for NaNs. But
          // for a NaN x != x is true, and we use this to detect them.
          super.visitInsn(Opcodes.DUP);
          super.visitInsn(Opcodes.DUP);
          super.visitInsn(Opcodes.FCMPG);
          super.visitJumpInsn(Opcodes.IFEQ, label);
          super.visitInsn(Opcodes.POP);
          super.visitInsn(Opcodes.FCONST_0);
          super.visitLabel(label);
          super.visitInsn(Opcodes.FCONST_1);
          super.visitInsn(Opcodes.FADD);
          super.visitInsn(Opcodes.FNEG);
          super.visitInsn(Opcodes.FRETURN);

        }
        break;
      case Opcodes.DRETURN:

        if (shouldMutate("primitive double", "-(x + 1)")) {
          // Strategy translated from jumble BCEL code
          // The following is complicated by the problem of NaNs. By default
          // the new value is -(x + 1), but this doesn't work for NaNs. But
          // for a NaN x != x is true, and we use this to detect them.
          super.visitInsn(Opcodes.DUP2);
          super.visitInsn(Opcodes.DUP2);
          super.visitInsn(Opcodes.DCMPG);
          super.visitJumpInsn(Opcodes.IFEQ, label);
          super.visitInsn(Opcodes.POP2);
          super.visitInsn(Opcodes.DCONST_0);
          super.visitLabel(label);
          super.visitInsn(Opcodes.DCONST_1);
          super.visitInsn(Opcodes.DADD);
          super.visitInsn(Opcodes.DNEG);
          super.visitInsn(Opcodes.DRETURN);

        }
        break;

      default:
        super.visitInsn(opcode);
        break;
      }

    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pitest.mutationtest.engine.gregor.MethodMutatorFactory#create(org.pitest
   * .mutationtest.engine.gregor.Context,
   * org.pitest.mutationtest.engine.gregor.MethodInfo,
   * org.objectweb.asm.MethodVisitor)
   */
  public MethodVisitor create(final Context context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    final ReturnValuesMethodVisitor visitor = new ReturnValuesMethodVisitor(
        context, methodVisitor);

    return new LineTrackingMethodAdapter(methodInfo, context, visitor);
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * org.pitest.mutationtest.engine.gregor.MethodMutatorFactory#getGloballyUniqueId
   * ()
   */
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

}
