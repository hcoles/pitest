package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Replaces bitwise "and" and "or" by the first member
 */
public enum OBBN2Mutator implements MethodMutatorFactory  {

    OBBN_2_MUTATOR;

    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor)  {
        return new OBBNMethodVisitor2(this, context, methodInfo, methodVisitor);
    }

    public String getGloballyUniqueId()  {
        return this.getClass().getName();
    }

    public String getName()  {
        return name();
    }
}

class OBBNMethodVisitor2 extends MethodVisitor  {

    private final MethodMutatorFactory factory;
    private final MutationContext      context;
    private final MethodInfo      info;

    OBBNMethodVisitor2(final MethodMutatorFactory factory,
                       final MutationContext context, final MethodInfo info, final MethodVisitor delegateMethodVisitor)  {
        super(ASMVersion.ASM_VERSION, delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
        this.info = info;
    }

    private boolean shouldMutate(String expression) {
        if (info.isGeneratedEnumMethod()) {
            return false;
        } else {
            final MutationIdentifier newId = this.context.registerMutation(
                    this.factory, "Replaced " + expression + " by first member");
            return this.context.shouldMutate(newId);
        }

    }

    @Override
    public void visitInsn(int opcode) {
        switch (opcode) {
            case Opcodes.IAND:
                if (this.shouldMutate("integer and"))  {
                    mv.visitInsn(Opcodes.POP);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.IOR:
                if (this.shouldMutate("integer or"))  {
                    mv.visitInsn(Opcodes.POP);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.LAND:
                if (this.shouldMutate("long and"))  {
                    mv.visitInsn(Opcodes.POP2);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.LOR:
                if (this.shouldMutate("long or"))  {
                    mv.visitInsn(Opcodes.POP2);
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