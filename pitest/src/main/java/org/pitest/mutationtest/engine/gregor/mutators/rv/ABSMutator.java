package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutator designed to insert (-) in front of Integer, Float, Long, Double
 * variable values.
 *
 * Applicable for array values, static and non static fields and local
 * variables.
 */
public enum ABSMutator implements MethodMutatorFactory {

    ABS_MUTATOR;

    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
                                final MethodVisitor methodVisitor) {
        return new ABSMethodVisitor(this, context, methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return name();
    }
}

class ABSMethodVisitor extends MethodVisitor {

    private final MethodMutatorFactory factory;
    private final MutationContext context;

    ABSMethodVisitor(final MethodMutatorFactory factory, final MutationContext context,
                     final MethodVisitor delegateMethodVisitor) {
        super(ASMVersion.ASM_VERSION, delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
    }

    private boolean shouldMutate(String message) {
        if (this.context.getClassInfo().isEnum()) {
            return false;
        }
        final MutationIdentifier newId = this.context.registerMutation(this.factory, message);
        return this.context.shouldMutate(newId);
    }

    // Local variables
    @Override
    public void visitVarInsn(int opcode, int var) {
        mv.visitVarInsn(opcode, var); // push the variable.

        switch (opcode) {
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

    // Fields, static or not.
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {

        mv.visitFieldInsn(opcode, owner, name, desc);

        // non static
        if (opcode == Opcodes.GETFIELD) {
            if (desc.equals("I")) {
                if (this.shouldMutate("Negated integer field " + name)) {
                    mv.visitInsn(Opcodes.INEG);
                    return;
                }
            }
            if (desc.equals("F")) {
                if (this.shouldMutate("Negated float field " + name)) {
                    mv.visitInsn(Opcodes.FNEG);
                    return;
                }
            }
            if (desc.equals("J")) {
                if (this.shouldMutate("Negated long field " + name)) {
                    mv.visitInsn(Opcodes.LNEG);
                    return;
                }
            }
            if (desc.equals("D")) {
                if (this.shouldMutate("Negated double field " + name)) {
                    mv.visitInsn(Opcodes.DNEG);
                    return;
                }
            }
            if (desc.equals("B")) {
                if (this.shouldMutate("Negated byte field " + name)) {
                    mv.visitInsn(Opcodes.INEG);
                    mv.visitInsn(Opcodes.I2B);
                    return;
                }
            }
            if (desc.equals("S")) {
                if (this.shouldMutate("Negated short field " + name)) {
                    mv.visitInsn(Opcodes.INEG);
                    mv.visitInsn(Opcodes.I2S);
                    return;
                }
            }
        }

        // static
        if (opcode == Opcodes.GETSTATIC) {
            if (desc.equals("I")) {
                if (this.shouldMutate("Negated integer static field " + name)) {
                    mv.visitInsn(Opcodes.INEG);
                    return;
                }
            }
            if (desc.equals("F")) {
                if (this.shouldMutate("Negated float static field " + name)) {
                    mv.visitInsn(Opcodes.FNEG);
                    return;
                }
            }
            if (desc.equals("J")) {
                if (this.shouldMutate("Negated long static field " + name)) {
                    mv.visitInsn(Opcodes.LNEG);
                    return;
                }
            }
            if (desc.equals("D")) {
                if (this.shouldMutate("Negated double static field " + name)) {
                    mv.visitInsn(Opcodes.DNEG);
                    return;
                }
            }
            if (desc.equals("B")) {
                if (this.shouldMutate("Negated byte static field " + name)) {
                    mv.visitInsn(Opcodes.INEG);
                    mv.visitInsn(Opcodes.I2B);
                    return;
                }
            }
            if (desc.equals("S")) {
                if (this.shouldMutate("Negated short static field " + name)) {
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
            // ARRAYS I F L D , loading
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