package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutator that replaces (a+b) by a;
 * Does the same for the operators (*,/,%,-).
 */
public enum AOD1Mutator implements MethodMutatorFactory  {

    AOD_1_MUTATOR;

    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor)  {
        return new AODMethodVisitor1(this, context, methodInfo, methodVisitor);
    }

    public String getGloballyUniqueId()  {
        return this.getClass().getName();
    }

    public String getName()  {
        return name();
    }
}

class AODMethodVisitor1 extends MethodVisitor  {

    private final MethodMutatorFactory factory;
    private final MutationContext      context;
    private final MethodInfo      info;

    AODMethodVisitor1(final MethodMutatorFactory factory,
                      final MutationContext context, final MethodInfo info, final MethodVisitor delegateMethodVisitor)  {
        super(ASMVersion.ASM_VERSION, delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
        this.info = info;
    }

    private boolean shouldMutate(String expression) {
        if (this.info.isGeneratedEnumMethod()) {
            return false;
        } else {
            final MutationIdentifier newId = this.context.registerMutation(
                    this.factory, "Replaced " + expression + " operation with first member");
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
                if (this.shouldMutate("integer"))  {
                    mv.visitInsn(Opcodes.POP);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.FADD:
            case Opcodes.FSUB:
            case Opcodes.FMUL:
            case Opcodes.FDIV:
            case Opcodes.FREM:
                if (this.shouldMutate("float"))  {
                    mv.visitInsn(Opcodes.POP);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.LADD:
            case Opcodes.LSUB:
            case Opcodes.LMUL:
            case Opcodes.LDIV:
            case Opcodes.LREM:
                if (this.shouldMutate("long"))  {
                    mv.visitInsn(Opcodes.POP2);
                } else  {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.DADD:
            case Opcodes.DSUB:
            case Opcodes.DMUL:
            case Opcodes.DDIV:
            case Opcodes.DREM:
                if (this.shouldMutate("double"))  {
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
