package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutation operator changing (a) to (++a)
 */
public enum UOI3Mutator implements MethodMutatorFactory {

    UOI_3_MUTATOR;

    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo,
                                final MethodVisitor methodVisitor) {
        return new UOIMethodVisitor3(this, context,  methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return name();
    }
}

class UOIMethodVisitor3 extends MethodVisitor {

    private final MethodMutatorFactory factory;
    private final MutationContext context;

    UOIMethodVisitor3(final MethodMutatorFactory factory, final MutationContext context,
                      final MethodVisitor delegateMethodVisitor) {
        super(ASMVersion.ASM_VERSION, delegateMethodVisitor);
        this.factory = factory;
        this.context = context;
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
                if (this.shouldMutate("Incremented (++a) integer local variable number " + var)) {
                    mv.visitIincInsn(var, 1);
                }
                mv.visitVarInsn(opcode, var);
                break;
            case Opcodes.FLOAD:
                if (this.shouldMutate("Incremented (++a) float local variable number " + var)) {
                    mv.visitVarInsn(opcode, var);
                    mv.visitInsn(Opcodes.FCONST_1);
                    mv.visitInsn(Opcodes.FADD);
                    mv.visitVarInsn(Opcodes.FSTORE, var);
                }
                mv.visitVarInsn(opcode, var);
                break;
            case Opcodes.LLOAD:
                if (this.shouldMutate("Incremented (++a) long local variable number " + var)) {
                    mv.visitVarInsn(opcode, var);
                    mv.visitInsn(Opcodes.LCONST_1);
                    mv.visitInsn(Opcodes.LADD);
                    mv.visitVarInsn(Opcodes.LSTORE, var);
                }
                mv.visitVarInsn(opcode, var);
                break;
            case Opcodes.DLOAD:
                if (this.shouldMutate("Incremented (++a) double local variable number " + var)) {
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

    // ARRAYS
    @Override
    public void visitInsn(final int opcode) {
        // I F L D + BS
        switch (opcode) {
            case Opcodes.IALOAD:
                if (this.shouldMutate("Incremented (++a) integer array field")) {
                    mv.visitInsn(Opcodes.DUP2); // stack = ... [array] [index] [array] [index]
                    mv.visitInsn(opcode); // stack = ... [array] [index] [array[index]]
                    mv.visitInsn(Opcodes.ICONST_1); // stack = ... [array] [index] [array[index]] [1]
                    mv.visitInsn(Opcodes.IADD); // stack = ... [array] [index] [array[index]+1]
                    mv.visitInsn(Opcodes.DUP_X2); // stack = ... [array[index]+1] [array] [index] [array[index]+1]
                    mv.visitInsn(Opcodes.IASTORE); // stack = ... [array[index]+1]
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.FALOAD:
                if (this.shouldMutate("Incremented (++a) float array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.FCONST_1);
                    mv.visitInsn(Opcodes.FADD);
                    mv.visitInsn(Opcodes.DUP_X2);
                    mv.visitInsn(Opcodes.FASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.LALOAD:
                if (this.shouldMutate("Incremented (++a) long array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.LCONST_1);
                    mv.visitInsn(Opcodes.LADD);
                    mv.visitInsn(Opcodes.DUP2_X2);
                    mv.visitInsn(Opcodes.LASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.DALOAD:
                if (this.shouldMutate("Incremented (++a) double array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.DCONST_1);
                    mv.visitInsn(Opcodes.DADD);
                    mv.visitInsn(Opcodes.DUP2_X2);
                    mv.visitInsn(Opcodes.DASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;

            case Opcodes.BALOAD:
                if (this.shouldMutate("Incremented (++a) byte array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IADD);
                    mv.visitInsn(Opcodes.I2B);
                    mv.visitInsn(Opcodes.DUP_X2);
                    mv.visitInsn(Opcodes.BASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;

            case Opcodes.SALOAD:
                if (this.shouldMutate("Incremented (++a) short array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IADD);
                    mv.visitInsn(Opcodes.I2S);
                    mv.visitInsn(Opcodes.DUP_X2);
                    mv.visitInsn(Opcodes.SASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;

            default:
                mv.visitInsn(opcode);
                break;
        }
    }

    // PARAMETERS, static or not.
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        // GETFIELD I,F,L,D + B,S
        if ((opcode == Opcodes.GETFIELD)) {
            if (desc.equals("I")) {
                if (this.shouldMutate("Incremented (++a) integer field " + name)) {
                    mv.visitInsn(Opcodes.DUP); // stack = .. [ref] [ref]
                    mv.visitFieldInsn(opcode, owner, name, desc); // stack = ... [ref] [ref.field]
                    mv.visitInsn(Opcodes.ICONST_1); // stack = ... [ref] [ref.field] [1]
                    mv.visitInsn(Opcodes.IADD); // stack = ... [ref] [ref.field + 1]
                    mv.visitInsn(Opcodes.DUP_X1); // stack = ... [ref.field +1] [ref] [ref.field +1]
                    mv.visitFieldInsn(Opcodes.PUTFIELD, owner, name, desc); // stack = ... [ref.field +1]
                    return;
                }
            }
            if (desc.equals("F")) {
                if (this.shouldMutate("Incremented (++a) float field " + name)) {
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
                if (this.shouldMutate("Incremented (++a) long field " + name)) {
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
                if (this.shouldMutate("Incremented (++a) double field " + name)) {
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
                if (this.shouldMutate("Incremented (++a) byte field " + name)) {
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
                if (this.shouldMutate("Incremented (++a) short field " + name)) {
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

        // GETSTATIC I,F,L,D + B,S
        if (opcode == Opcodes.GETSTATIC) {
            if (desc.equals("I")) {
                if (this.shouldMutate("Incremented (++a) static integer field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IADD);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("F")) {
                if (this.shouldMutate("Incremented (++a) static float field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.FCONST_1);
                    mv.visitInsn(Opcodes.FADD);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("J")) {
                if (this.shouldMutate("Incremented (++a) static long field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.LCONST_1);
                    mv.visitInsn(Opcodes.LADD);
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("D")) {
                if (this.shouldMutate("Incremented (++a) static double field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DCONST_1);
                    mv.visitInsn(Opcodes.DADD);
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("B")) {
                if (this.shouldMutate("Incremented (++a) static byte field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IADD);
                    mv.visitInsn(Opcodes.I2B);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("S")) {
                if (this.shouldMutate("Incremented (++a) static short field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.IADD);
                    mv.visitInsn(Opcodes.I2S);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }

}
