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
package org.pitest.mutationtest.engine.gregor.mutators;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.util.PitError;

/**
 * The <code>InlineConstantMutator</code> is a mutator that mutates integer
 * inline constants (including short, byte, long) by adding 1 and that mutates
 * float inline constants (including double) by replacing them with 1.
 *
 *
 * @author Stefan Penndorf &lt;stefan.penndorf@gmail.com&gt;
 */
public class InlineConstantMutator implements MethodMutatorFactory {

  private final class InlineConstantVisitor extends MethodVisitor {
    private final MutationContext context;

    InlineConstantVisitor(final MutationContext context,
        final MethodVisitor delegateVisitor) {
      super(ASMVersion.ASM_VERSION, delegateVisitor);
      this.context = context;
    }

    private boolean mutate(final Double constant) {
      // avoid addition to floating points as may yield same value

      final Double replacement = (constant == 1D) ? 2D : 1D;

      if (shouldMutate(constant, replacement)) {
        translateToByteCode(replacement);
        return true;
      }

      return false;
    }

    private boolean mutate(final Float constant) {
      // avoid addition to floating points as may yield same value

      final Float replacement = (constant == 1F) ? 2F : 1F;

      if (shouldMutate(constant, replacement)) {
        translateToByteCode(replacement);
        return true;
      }

      return false;
    }

    private boolean mutate(final Integer constant) {
      final Integer replacement;

      switch (constant.intValue()) {
      case 1:
        replacement = 0;
        break;
      case Byte.MAX_VALUE:
        replacement = (int) Byte.MIN_VALUE;
        break;
      case Short.MAX_VALUE:
        replacement = (int) Short.MIN_VALUE;
        break;
      default:
        replacement = constant + 1;
        break;
      }

      if (shouldMutate(constant, replacement)) {
        translateToByteCode(replacement);
        return true;
      }

      return false;
    }

    private boolean mutate(final Long constant) {

      final Long replacement = constant + 1L;

      if (shouldMutate(constant, replacement)) {
        translateToByteCode(replacement);
        return true;
      }

      return false;

    }

    private boolean mutate(final Number constant) {

      if (constant instanceof Integer) {
        return mutate((Integer) constant);
      } else if (constant instanceof Long) {
        return mutate((Long) constant);
      } else if (constant instanceof Float) {
        return mutate((Float) constant);
      } else if (constant instanceof Double) {
        return mutate((Double) constant);
      } else {
        throw new PitError("Unsupported subtype of Number found:"
            + constant.getClass());
      }

    }

    private <T extends Number> boolean shouldMutate(final T constant,
        final T replacement) {
      final MutationIdentifier mutationId = this.context.registerMutation(
          InlineConstantMutator.this, "Substituted " + constant + " with "
              + replacement);

      return this.context.shouldMutate(mutationId);
    }

    private void translateToByteCode(final Double constant) {
      if (constant == 0D) {
        super.visitInsn(Opcodes.DCONST_0);
      } else if (constant == 1D) {
        super.visitInsn(Opcodes.DCONST_1);
      } else {
        super.visitLdcInsn(constant);
      }
    }

    private void translateToByteCode(final Float constant) {
      if (constant == 0.0F) {
        super.visitInsn(Opcodes.FCONST_0);
      } else if (constant == 1.0F) {
        super.visitInsn(Opcodes.FCONST_1);
      } else if (constant == 2.0F) {
        super.visitInsn(Opcodes.FCONST_2);
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

    private void translateToByteCode(final Long constant) {
      if (constant == 0L) {
        super.visitInsn(Opcodes.LCONST_0);
      } else if (constant == 1L) {
        super.visitInsn(Opcodes.LCONST_1);
      } else {
        super.visitLdcInsn(constant);
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
    private Number translateToNumber(final int opcode) {
      switch (opcode) {
      case Opcodes.ICONST_M1:
        return -1;
      case Opcodes.ICONST_0:
        return 0;
      case Opcodes.ICONST_1:
        return 1;
      case Opcodes.ICONST_2:
        return 2;
      case Opcodes.ICONST_3:
        return 3;
      case Opcodes.ICONST_4:
        return 4;
      case Opcodes.ICONST_5:
        return 5;
      case Opcodes.LCONST_0:
        return 0L;
      case Opcodes.LCONST_1:
        return 1L;
      case Opcodes.FCONST_0:
        return 0F;
      case Opcodes.FCONST_1:
        return 1F;
      case Opcodes.FCONST_2:
        return 2F;
      case Opcodes.DCONST_0:
        return 0D;
      case Opcodes.DCONST_1:
        return 1D;
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

      if (!mutate(inlineConstant) ) {
        super.visitInsn(opcode);
      }
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodAdapter#visitIntInsn(int, int)
     */
    @Override
    public void visitIntInsn(final int opcode, final int operand) {
      if ((opcode == Opcodes.BIPUSH) || (opcode == Opcodes.SIPUSH)) {
        if (mutate(operand) ) {
          return;
        }
      }

      super.visitIntInsn(opcode, operand);
    }

    /*
     * (non-Javadoc)
     *
     * @see org.objectweb.asm.MethodAdapter#visitLdcInsn(java.lang.Object)
     */
    @Override
    public void visitLdcInsn(final Object constant) {
      // do not mutate strings or .class here
      if (constant instanceof Number) {
        if (mutate((Number) constant)) {
            return;
        }
      }

      super.visitLdcInsn(constant);
    }

  }

  @Override
  public MethodVisitor create(final MutationContext context,
      final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
    return new InlineConstantVisitor(context, methodVisitor);
  }

  @Override
  public String getGloballyUniqueId() {
    return this.getClass().getName();
  }

  @Override
  public String toString() {
    return "INLINE_CONSTS";
  }

  @Override
  public String getName() {
    return toString();
  }

}
