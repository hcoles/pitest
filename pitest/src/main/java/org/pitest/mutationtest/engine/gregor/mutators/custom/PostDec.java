package org.pitest.mutationtest.engine.gregor.mutators.custom;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public enum PostDec implements MethodMutatorFactory {

    POST_DEC;

    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new PostDecVisitor(this, context, methodInfo, methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return name();
    }
}

class PostDecVisitor extends MethodVisitor {

    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private final MethodInfo info;

    PostDecVisitor(final MethodMutatorFactory factory, final MutationContext context, final MethodInfo info,
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
        mv.visitVarInsn(opcode, var);
        switch (opcode) {
        case Opcodes.ILOAD:
            if (this.shouldMutate("Post Decrement Mutator")) {
                mv.visitIincInsn(var, -1);
            }
            break;
        case Opcodes.FLOAD:
            if (this.shouldMutate("Post Decrement Mutator")) {
                mv.visitInsn(Opcodes.DUP);
                mv.visitInsn(Opcodes.FCONST_1);
                mv.visitInsn(Opcodes.FSUB);
                mv.visitVarInsn(Opcodes.FSTORE, var);
            }
            break;
        case Opcodes.LLOAD:
            if (this.shouldMutate("Post Decrement Mutator")) {
                mv.visitInsn(Opcodes.DUP2);
                mv.visitInsn(Opcodes.LCONST_1);
                mv.visitInsn(Opcodes.LSUB);
                mv.visitVarInsn(Opcodes.LSTORE, var);
            }
            break;
        case Opcodes.DLOAD:
            if (this.shouldMutate("Post Decrement Mutator")) {
                mv.visitInsn(Opcodes.DUP2);
                mv.visitInsn(Opcodes.DCONST_1);
                mv.visitInsn(Opcodes.DSUB);
                mv.visitVarInsn(Opcodes.DSTORE, var);
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

        if ((opcode == Opcodes.GETFIELD)) {
            if (desc.equals("I")) {
                if (this.shouldMutate("Post Decrement Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP_X1);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.ISUB);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("F")) {
                if (this.shouldMutate("Post Decrement Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP_X1);
                    mv.visitInsn(Opcodes.FCONST_1);
                    mv.visitInsn(Opcodes.FSUB);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("J")) {
                if (this.shouldMutate("Post Decrement Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP2_X1);
                    mv.visitInsn(Opcodes.LCONST_1);
                    mv.visitInsn(Opcodes.LSUB);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("D")) {
                if (this.shouldMutate("Post Decrement Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP2_X1);
                    mv.visitInsn(Opcodes.DCONST_1);
                    mv.visitInsn(Opcodes.DSUB);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("B")) {
                if (this.shouldMutate("Post Decrement Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP_X1);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.ISUB);
                    mv.visitInsn(Opcodes.I2B);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("S")) {
                if (this.shouldMutate("Post Decrement Mutator")) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP_X1);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.ISUB);
                    mv.visitInsn(Opcodes.I2S);
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc);
                    return;
                }
            }
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }
}
