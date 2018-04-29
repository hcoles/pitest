package org.pitest.mutationtest.engine.gregor.mutators.custom;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
public enum NegationMutator implements MethodMutatorFactory
{

    NEGATION_MUTATOR;
    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo, final MethodVisitor methodVisitor)
    {
        return new NegationMethodVisitor(this, context, methodInfo, methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return name();
    }
}

class NegationMethodVisitor extends MethodVisitor {

    private final MethodMutatorFactory factory;
    private final MutationContext context;
    private final MethodInfo info;

    NegationMethodVisitor(final MethodMutatorFactory factory, final MutationContext context, final MethodInfo info,
            final MethodVisitor delegateMethodVisitor) {
        super(Opcodes.ASM5, delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
        this.info = info;
    }

    private boolean shouldMutate(String message) {
        if (context.getClassInfo().isEnum()) {
            return false;
        } else {
            final MutationIdentifier newId = this.context.registerMutation(this.factory, message);
            return this.context.shouldMutate(newId);
        }
    }

    @Override
    public void visitVarInsn(int opcode, int var) {
        mv.visitVarInsn(opcode, var);
        
        switch (opcode)
        {
        case Opcodes.ILOAD:
            if (this.shouldMutate("Negated integer local variable number " + var)) {
                mv.visitInsn(Opcodes.INEG);
            }
            break;

        case Opcodes.FLOAD:
            if (this.shouldMutate("Negated float local variable number " + var)) {
                mv.visitInsn(Opcodes.FNEG);
            }
            break;

        case Opcodes.LLOAD:
            if (this.shouldMutate("Negated long local variable number " + var)) {
                mv.visitInsn(Opcodes.LNEG);
            }
            break;

        case Opcodes.DLOAD:
            if (this.shouldMutate("Negated double local variable number " + var)) {
                mv.visitInsn(Opcodes.DNEG);
            }
            break;
        default:
            break;
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

        mv.visitFieldInsn(opcode, owner, name, desc);
        if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {


            switch(desc)
            {
                case "I":
                    if (this.shouldMutate("Negated Integer " + name)) {
                    mv.visitInsn(Opcodes.INEG);
                    return;
                }
                case "F":
                    if (this.shouldMutate("Negated Float field " + name)) {
                        mv.visitInsn(Opcodes.FNEG);
                        return;
                    }
                case "J":
                    if (this.shouldMutate("Negated Long field " + name)) {
                        mv.visitInsn(Opcodes.LNEG);
                        return;
                    }
                case "D":
                if (this.shouldMutate("Negated Double field " + name)) {
                    mv.visitInsn(Opcodes.DNEG);
                    return;
                }
                case "B":
                if (this.shouldMutate("Negated Byte field " + name)) {
                    mv.visitInsn(Opcodes.INEG);
                    mv.visitInsn(Opcodes.I2B);
                    return;
                }
                case "S":
                if (this.shouldMutate("Negated Short field " + name)) {
                    mv.visitInsn(Opcodes.INEG);
                    mv.visitInsn(Opcodes.I2S);
                    return;
                }
            }
        }
    }

    @Override
    public void visitInsn(final int opcode) {
        mv.visitInsn(opcode);        
        switch (opcode) {

        case Opcodes.IALOAD:
            if (this.shouldMutate("Negated integer array field")) {
                mv.visitInsn(Opcodes.INEG);
            }
            break;

        case Opcodes.FALOAD:
            if (this.shouldMutate("Negated float array field")) {
                mv.visitInsn(Opcodes.FNEG);
            }
            break;

        case Opcodes.LALOAD:
            if (this.shouldMutate("Negated long array field")) {
                mv.visitInsn(Opcodes.LNEG);
            }
            break;

        case Opcodes.DALOAD:
            if (this.shouldMutate("Negated double array field")) {
                mv.visitInsn(Opcodes.DNEG);
            }
            break;

        case Opcodes.BALOAD:
            if (this.shouldMutate("Negated byte array field")) {
                mv.visitInsn(Opcodes.INEG);
                mv.visitInsn(Opcodes.I2B);
            }
            break;

        case Opcodes.SALOAD:
            if (this.shouldMutate("Negated short array field")) {
                mv.visitInsn(Opcodes.INEG);
                mv.visitInsn(Opcodes.I2S);
            }
            break;

        default:
            break;
        }
    }
}
