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
 * Replaces bitwise "and" and "or" by the second member
 */
public enum OBBN3Mutator implements MethodMutatorFactory  {

    OBBN_3_MUTATOR;

    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor)  {
        return new OBBNMethodVisitor3(this, context, methodInfo, methodVisitor);
    }

    public String getGloballyUniqueId()  {
        return this.getClass().getName();
    }

    public String getName()  {
        return name();
    }
}

class OBBNMethodVisitor3 extends LocalVariablesSorter  {

    private final MethodMutatorFactory factory;
    private final MutationContext      context;
    private final MethodInfo      info;

    OBBNMethodVisitor3(final MethodMutatorFactory factory,
                       final MutationContext context, final MethodInfo info, final MethodVisitor delegateMethodVisitor)  {
        super(ASMVersion.ASM_VERSION, info.getAccess(), info.getMethodDescriptor(), delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
        this.info = info;
    }

    private boolean shouldMutate(String expression) {
        if (info.isGeneratedEnumMethod()) {
            return false;
        } else {
            final MutationIdentifier newId = this.context.registerMutation(
                    this.factory, "Replace " + expression + " by second member");
            return this.context.shouldMutate(newId);
        }

    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
            case Opcodes.IAND:
                if (this.shouldMutate("integer and"))  {
                    int storage = this.newLocal(Type.INT_TYPE);
                    mv.visitVarInsn(Opcodes.ISTORE,storage);
                    mv.visitInsn(Opcodes.POP);
                    mv.visitVarInsn(Opcodes.ILOAD,storage);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.IOR:
                if (this.shouldMutate("integer or"))  {
                    int storage = this.newLocal(Type.INT_TYPE);
                    mv.visitVarInsn(Opcodes.ISTORE,storage);
                    mv.visitInsn(Opcodes.POP);
                    mv.visitVarInsn(Opcodes.ILOAD,storage);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.LAND:
                if (this.shouldMutate("long and"))  {
                    int storage = this.newLocal(Type.LONG_TYPE);
                    mv.visitVarInsn(Opcodes.LSTORE,storage);
                    mv.visitInsn(Opcodes.POP2);
                    mv.visitVarInsn(Opcodes.LLOAD,storage);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.LOR:
                if (this.shouldMutate("long or"))  {
                    int storage = this.newLocal(Type.LONG_TYPE);
                    mv.visitVarInsn(Opcodes.LSTORE,storage);
                    mv.visitInsn(Opcodes.POP2);
                    mv.visitVarInsn(Opcodes.LLOAD,storage);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            default:
                mv.visitInsn(opcode);
                break;
        }
    }
}