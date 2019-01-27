package org.pitest.mutationtest.engine.gregor.mutators.rv;


import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.util.PitError;

public abstract class AbstractCRCRVisitor extends MethodVisitor {
    final MutationContext context;
    final MethodMutatorFactory factory;

    AbstractCRCRVisitor(final MutationContext context,
                        final MethodVisitor delegateVisitor, final MethodMutatorFactory factory) {
        super(ASMVersion.ASM_VERSION, delegateVisitor);
        this.context = context;
        this.factory = factory;
    }

    abstract void mutate(Double constant);

    abstract void mutate(Float constant);

    abstract void mutate(Integer constant);

    abstract void mutate(Long constant);

    void mutate(final Number constant) {

        if (constant instanceof Integer) {
            mutate((Integer) constant);
        } else if (constant instanceof Long) {
            mutate((Long) constant);
        } else if (constant instanceof Float) {
            mutate((Float) constant);
        } else if (constant instanceof Double) {
            mutate((Double) constant);
        } else {
            throw new PitError("Unsupported subtype of Number found:"
                    + constant.getClass());
        }

    }

    <T extends Number> boolean shouldMutate(final T constant,
                                                    final T replacement) {
        final MutationIdentifier mutationId = this.context.registerMutation(
                this.factory, "Substituted " + constant + " with "
                        + replacement);

        return this.context.shouldMutate(mutationId);
    }

    void translateToByteCode(final Double constant) {
        if (constant == 0D) {
            super.visitInsn(Opcodes.DCONST_0);
        } else if (constant == 1D) {
            super.visitInsn(Opcodes.DCONST_1);
        } else {
            super.visitLdcInsn(constant);
        }
    }

    void translateToByteCode(final Float constant) {
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

    void translateToByteCode(final Integer constant) {
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

    void translateToByteCode(final Long constant) {
        if (constant == 0L) {
            super.visitInsn(Opcodes.LCONST_0);
        } else if (constant == 1L) {
            super.visitInsn(Opcodes.LCONST_1);
        } else {
            super.visitLdcInsn(constant);
        }
    }

    Number translateToNumber(final int opcode) {
        switch (opcode) {
            case Opcodes.ICONST_M1:
                return Integer.valueOf(-1);
            case Opcodes.ICONST_0:
                return Integer.valueOf(0);
            case Opcodes.ICONST_1:
                return Integer.valueOf(1);
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
            case Opcodes.FCONST_0:
                return Float.valueOf(0F);
            case Opcodes.FCONST_1:
                return Float.valueOf(1F);
            case Opcodes.FCONST_2:
                return Float.valueOf(2F);
            case Opcodes.DCONST_0:
                return Double.valueOf(0D);
            case Opcodes.DCONST_1:
                return Double.valueOf(1D);
            default:
                return null;
        }
    }

    @Override
    public void visitInsn(final int opcode) {

        final Number inlineConstant = translateToNumber(opcode);

        if (inlineConstant == null) {
            super.visitInsn(opcode);
            return;
        }

        mutate(inlineConstant);
    }

    @Override
    public void visitIntInsn(final int opcode, final int operand) {
        if ((opcode == Opcodes.BIPUSH) || (opcode == Opcodes.SIPUSH)) {
            mutate(operand);
        } else {
            super.visitIntInsn(opcode, operand);
        }
    }

    @Override
    public void visitLdcInsn(final Object constant) {
        // do not mutate strings or .class here
        if (constant instanceof Number) {
            mutate((Number) constant);
        } else {
            super.visitLdcInsn(constant);
        }
    }

}