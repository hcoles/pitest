package org.pitest.mutationtest.engine.gregor.mutators.custom;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

public enum M1 implements MethodMutatorFactory {

    M1_MUTATOR;

    @Override
    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo, final MethodVisitor methodVisitor)
    {
        return new M1Visitor(this, context, methodVisitor);
    }

    @Override
    public String getGloballyUniqueId()
    {
        return this.getClass().getName();
    }
    
    @Override
    public String getName()
    {
        return name();
    }
}

class M1Visitor extends MethodVisitor
{

    final MutationContext context;
    final MethodMutatorFactory factory;

    public M1Visitor(final MethodMutatorFactory factory, final MutationContext context, final MethodVisitor writer)
    {
        super(Opcodes.ASM6, writer);
        this.factory = factory;
        this.context = context;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc)
    {
        if (opcode == Opcodes.GETFIELD && shouldMutate(owner, name))
        {
            Label label1 = new Label();
            super.visitInsn(Opcodes.DUP);
            super.visitJumpInsn(Opcodes.IFNULL, label1);
            super.visitInsn(Opcodes.POP);
            switch (desc)
            {
                case "Z":
                case "C":
                case "B":
                case "S":
                case "I":
                    super.visitInsn(Opcodes.ICONST_0);
                    break;
                case "F":
                    super.visitInsn(Opcodes.FCONST_0);
                    break;
                case "J":
                    super.visitInsn(Opcodes.LCONST_0);
                    break;
                case "D":
                    super.visitInsn(Opcodes.DCONST_0);
                    break;
                default:
                    super.visitInsn(Opcodes.ACONST_NULL);
            }
            Label label2 = new Label();
            super.visitJumpInsn(Opcodes.GOTO, label2);
            super.visitLabel(label1);
            super.visitFieldInsn(opcode, owner, name, desc);
            super.visitLabel(label2);

        }
        else
        {
            super.visitFieldInsn(opcode, owner, name, desc);
        }
    }

    private boolean shouldMutate(final String ownerName, final String fieldName)
    {
        final MutationIdentifier newId = this.context.registerMutation(
                this.factory, "[M1 Mutation Applied] Object Dereference detected on " + fieldName +" variable.");
        return this.context.shouldMutate(newId);
    }

}