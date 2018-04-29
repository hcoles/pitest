package org.pitest.mutationtest.engine.gregor.mutators.custom;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;


public enum PreInc implements MethodMutatorFactory {

    PRE_INC;

    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
            final MethodVisitor methodVisitor) {
        return new PreIncVisitor(this, context, methodInfo, methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return name();
    }
}

class PreIncVisitor extends MethodVisitor {

    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private final MethodInfo info;

    PreIncVisitor(final MethodMutatorFactory factory, final MutationContext context, final MethodInfo info,
            final MethodVisitor delegateMethodVisitor) {
        super(Opcodes.ASM5, delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
        this.info = info;
    }

    private boolean shouldMutate(String description) {
        if (context.getClassInfo().isEnum()) {
            return false;
        } else {
            final MutationIdentifier newId = this.context.registerMutation(this.factory, description);
            return this.context.shouldMutate(newId);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        switch (opcode) {
        case Opcodes.ILOAD:
            if (this.shouldMutate("Pre Increment Mutator")) {
                mv.visitIincInsn(var, 1);
            }
            mv.visitVarInsn(opcode, var);
            break;
        case Opcodes.FLOAD:
            if (this.shouldMutate("Pre Increment Mutator")) {
                mv.visitVarInsn(opcode, var);
                mv.visitInsn(Opcodes.FCONST_1);
                mv.visitInsn(Opcodes.FADD);
                mv.visitVarInsn(Opcodes.FSTORE, var);
            }
            mv.visitVarInsn(opcode, var);
            break;
        case Opcodes.LLOAD:
            if (this.shouldMutate("Pre Increment Mutator")) {
                mv.visitVarInsn(opcode, var);
                mv.visitInsn(Opcodes.LCONST_1);
                mv.visitInsn(Opcodes.LADD);
                mv.visitVarInsn(Opcodes.LSTORE, var);
            }
            mv.visitVarInsn(opcode, var);
            break;
        case Opcodes.DLOAD:
            if (this.shouldMutate("Pre Increment Mutator")) {
                mv.visitVarInsn(opcode, var);
                mv.visitInsn(Opcodes.DCONST_1);
                mv.visitInsn(Opcodes.DADD);
                mv.visitVarInsn(Opcodes.DSTORE, var);
            }
            mv.visitVarInsn(opcode, var);
            break;
        default:
            mv.visitVarInsn(opcode, var);
            break;
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

        if ((opcode == Opcodes.GETFIELD)) {
            if (desc.equals("I")) {
                if (this.shouldMutate("Pre Increment Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IADD);
                    mv.visitInsn(Opcodes.DUP_X1);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("F")) {
                if (this.shouldMutate("Pre Increment Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.FCONST_1);
                    mv.visitInsn(Opcodes.FADD);
                    mv.visitInsn(Opcodes.DUP_X1);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("J")) {
                if (this.shouldMutate("Pre Increment Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.LCONST_1);
                    mv.visitInsn(Opcodes.LADD);
                    mv.visitInsn(Opcodes.DUP2_X1);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("D")) {
                if (this.shouldMutate("Pre Increment Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DCONST_1);
                    mv.visitInsn(Opcodes.DADD);
                    mv.visitInsn(Opcodes.DUP2_X1);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("B")) {
                if (this.shouldMutate("Pre Increment Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IADD);
                    mv.visitInsn(Opcodes.I2B);
                    mv.visitInsn(Opcodes.DUP_X1);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("S")) {
                if (this.shouldMutate("Pre Increment Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IADD);
                    mv.visitInsn(Opcodes.I2S);
                    mv.visitInsn(Opcodes.DUP_X1);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

}
