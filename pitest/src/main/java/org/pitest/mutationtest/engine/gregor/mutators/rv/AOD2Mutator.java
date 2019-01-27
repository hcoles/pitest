package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutator that replaces (a+b) by b;
 * Does the same for the operators (*,/,%,-).
 */
public enum AOD2Mutator implements MethodMutatorFactory {

    AOD_2_MUTATOR;

    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new AODMethodVisitor2(this, context, methodInfo, methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return name();
    }
}

class AODMethodVisitor2 extends LocalVariablesSorter {

    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private final MethodInfo info;

    AODMethodVisitor2( final MethodMutatorFactory factory, final MutationContext context, final MethodInfo info,
                       final MethodVisitor delegateMethodVisitor) {
        super(ASMVersion.ASM_VERSION, info.getAccess(), info.getMethodDescriptor(), delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
        this.info = info;
    }

    private boolean shouldMutate(String expression) {
        if (this.info.isGeneratedEnumMethod()) {
            return false;
        } else {
            final MutationIdentifier newId = this.context.registerMutation(this.factory,
                    "Replaced " + expression + " operation by second member");
            return this.context.shouldMutate(newId);
        }

    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
            case Opcodes.IADD:
            case Opcodes.ISUB:
            case Opcodes.IMUL:
            case Opcodes.IDIV:
            case Opcodes.IREM:
                if (this.shouldMutate("integer")) {
                    int storage = this.newLocal(Type.INT_TYPE);
                    mv.visitVarInsn(Opcodes.ISTORE, storage);
                    mv.visitInsn(Opcodes.POP);
                    mv.visitVarInsn(Opcodes.ILOAD, storage);
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.FADD:
            case Opcodes.FSUB:
            case Opcodes.FMUL:
            case Opcodes.FDIV:
            case Opcodes.FREM:
                if (this.shouldMutate("float")) {
                    int storage = this.newLocal(Type.FLOAT_TYPE);
                    mv.visitVarInsn(Opcodes.FSTORE, storage);
                    mv.visitInsn(Opcodes.POP);
                    mv.visitVarInsn(Opcodes.FLOAD, storage);
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.LADD:
            case Opcodes.LSUB:
            case Opcodes.LMUL:
            case Opcodes.LDIV:
            case Opcodes.LREM:
                if (this.shouldMutate("long")) {
                    int storage = this.newLocal(Type.LONG_TYPE);
                    mv.visitVarInsn(Opcodes.LSTORE, storage);
                    mv.visitInsn(Opcodes.POP2);
                    mv.visitVarInsn(Opcodes.LLOAD, storage);
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.DADD:
            case Opcodes.DSUB:
            case Opcodes.DMUL:
            case Opcodes.DDIV:
            case Opcodes.DREM:
                if (this.shouldMutate("double")) {
                    int storage = this.newLocal(Type.DOUBLE_TYPE);
                    mv.visitVarInsn(Opcodes.DSTORE, storage);
                    mv.visitInsn(Opcodes.POP2);
                    mv.visitVarInsn(Opcodes.DLOAD, storage);
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            default:
                mv.visitInsn(opcode);
                break;
        }
    }
}
