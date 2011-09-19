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

import org.objectweb.asm.MethodAdapter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.Context;
import org.pitest.mutationtest.engine.gregor.LineTrackingMethodAdapter;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

/**
 * The <code>InlineConstantIncrementMutator</code> is a mutator that mutates
 * integer inline constants (including short, byte, long) by adding 1.
 * 
 * 
 * @author Stefan Penndorf <stefan.penndorf@gmail.com>
 */
public class InlineConstantIncrementMutator implements MethodMutatorFactory {

  private class InlineConstantVisitor extends MethodAdapter implements
      MethodVisitor {
    private final Context context;

    public InlineConstantVisitor(final Context context,
        final MethodVisitor delegateVisitor) {
      super(delegateVisitor);
      this.context = context;
    }

    private void mutate(final Integer constant) {

      final Integer replacement = constant + 1;

      MutationIdentifier mutationId = this.context.registerMutation(
          InlineConstantIncrementMutator.this, "Substituted " + constant
              + " with " + replacement);

      if (this.context.shouldMutate(mutationId)) {
        translateToByteCode(replacement);
      } else {
        translateToByteCode(constant);
      }
    }

    /**
     * @param inlineConstant
     */
    private void mutate(final Long constant) {

      final Long replacement = constant + 1;

      MutationIdentifier mutationId = this.context.registerMutation(
          InlineConstantIncrementMutator.this, "Substituted " + constant
              + " with " + replacement);

      if (this.context.shouldMutate(mutationId)) {
        translateToByteCode(replacement);
      } else {
        translateToByteCode(constant);
      }

    }

    private void translateToByteCode(final Long constant) {
      if (constant == 0L) {
        super.visitInsn(Opcodes.LCONST_0);
      } else if (constant == 1L) {
        super.visitInsn(Opcodes.LCONST_1);
      } else {
        super.visitLdcInsn(constant);
      }
    }

    private void translateToByteCode(final Integer constant) {
      switch (constant) {
      case -1:
        super.visitInsn(Opcodes.ICONST_M1);
        break;
      case 0:
        super.visitInsn(Opcodes.ICONST_0);
        break;
      case 1:
        super.visitInsn(Opcodes.ICONST_1);
        break;
      case 2:
        super.visitInsn(Opcodes.ICONST_2);
        break;
      case 3:
        super.visitInsn(Opcodes.ICONST_3);
        break;
      case 4:
        super.visitInsn(Opcodes.ICONST_4);
        break;
      case 5:
        super.visitInsn(Opcodes.ICONST_5);
        break;
      default:
        super.visitLdcInsn(constant);
        break;
      }
    }

    /**
     * Translates the opcode to a number (inline constant) if possible or
     * returns <code>null</code> if the opcode cannot be translated.
     * 
     * @param opcode
     *          that might represent an inline constant.
     * @return the value of the inline constant represented by opcode or
     *         <code>null</code> if the opcode does not represent a
     *         number/constant.
     */
    private Number translateToNumber(int opcode) {
      switch (opcode) {
      case Opcodes.ICONST_M1:
        return Integer.valueOf(-1);
      case Opcodes.ICONST_0:
        return Integer.valueOf(0);
      case Opcodes.ICONST_2:
        return Integer.valueOf(2);
      case Opcodes.ICONST_3:
        return Integer.valueOf(3);
      case Opcodes.ICONST_4:
        return Integer.valueOf(4);
      case Opcodes.ICONST_5:
        return Integer.valueOf(5);
      case Opcodes.LCONST_0:
        return Long.valueOf(0L);
      case Opcodes.LCONST_1:
        return Long.valueOf(1L);
      default:
        return null;
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.MethodAdapter#visitInsn(int)
     */
    @Override
    public void visitInsn(final int opcode) {

      final Number inlineConstant = translateToNumber(opcode);

      if (inlineConstant == null) {
        super.visitInsn(opcode);
        return;
      }

      if (inlineConstant instanceof Integer) {
        mutate((Integer) inlineConstant);
      } else if (inlineConstant instanceof Long) {
        mutate((Long) inlineConstant);
      } else {
        throw new RuntimeException("Unsupported subtype of Number found:"
            + inlineConstant.getClass());
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.MethodAdapter#visitIntInsn(int, int)
     */
    @Override
    public void visitIntInsn(int opcode, int operand) {
      if (opcode == Opcodes.BIPUSH || opcode == Opcodes.SIPUSH) {
        mutate(operand);
      } else {
        super.visitIntInsn(opcode, operand);
      }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.objectweb.asm.MethodAdapter#visitLdcInsn(java.lang.Object)
     */
    @Override
    public void visitLdcInsn(Object constant) {

      if (constant instanceof Integer) {
        mutate((Integer) constant);
      } else if (constant instanceof Long) {
        mutate((Long) constant);
      } else {
        super.visitLdcInsn(constant);
      }
    }

  }

  public MethodVisitor create(final Context context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    InlineConstantVisitor visitor = new InlineConstantVisitor(context,
        methodVisitor);

    return new LineTrackingMethodAdapter(methodInfo, context, visitor);
  }

  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

}
