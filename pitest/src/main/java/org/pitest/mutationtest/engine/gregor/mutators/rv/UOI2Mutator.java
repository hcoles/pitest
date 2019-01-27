package org.pitest.mutationtest.engine.gregor.mutators.rv;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Mutation operator changing (a) to (a--)
 */
public enum UOI2Mutator implements MethodMutatorFactory {

    UOI_2_MUTATOR;

    public MethodVisitor create(final MutationContext context, final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new UOI2MethodVisitor(this, context, methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return name();
    }
}

class UOI2MethodVisitor extends MethodVisitor {

    private final MethodMutatorFactory factory;
    private final MutationContext context;

    UOI2MethodVisitor(final MethodMutatorFactory factory, final MutationContext context,
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
        mv.visitVarInsn(opcode, var);
        switch (opcode) {
            case Opcodes.ILOAD:
                if (this.shouldMutate("Decremented (a--) integer local variable number " + var)) {
                    mv.visitIincInsn(var, -1);
                }
                break;
            case Opcodes.FLOAD:
                if (this.shouldMutate("Decremented (a--) float local variable number " + var)) {
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitInsn(Opcodes.FCONST_1);
                    mv.visitInsn(Opcodes.FSUB);
                    mv.visitVarInsn(Opcodes.FSTORE, var);
                }
                break;
            case Opcodes.LLOAD:
                if (this.shouldMutate("Decremented (a--) long local variable number " + var)) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(Opcodes.LCONST_1);
                    mv.visitInsn(Opcodes.LSUB);
                    mv.visitVarInsn(Opcodes.LSTORE, var);
                }
                break;
            case Opcodes.DLOAD:
                if (this.shouldMutate("Decremented (a--) double local variable number " + var)) {
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

    // ARRAYS
    @Override
    public void visitInsn(final int opcode) {
        // I F L D + BS
        switch (opcode) {
            case Opcodes.IALOAD:
                if (this.shouldMutate("Decremented (a--) integer array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.DUP_X2);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.ISUB);
                    mv.visitInsn(Opcodes.IASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.FALOAD:
                if (this.shouldMutate("Decremented (a--) float array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.DUP_X2);
                    mv.visitInsn(Opcodes.FCONST_1);
                    mv.visitInsn(Opcodes.FSUB);
                    mv.visitInsn(Opcodes.FASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.LALOAD:
                if (this.shouldMutate("Decremented (a--) long array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.DUP2_X2);
                    mv.visitInsn(Opcodes.LCONST_1);
                    mv.visitInsn(Opcodes.LSUB);
                    mv.visitInsn(Opcodes.LASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;
            case Opcodes.DALOAD:
                if (this.shouldMutate("Decremented (a--) double array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.DUP2_X2);
                    mv.visitInsn(Opcodes.DCONST_1);
                    mv.visitInsn(Opcodes.DSUB);
                    mv.visitInsn(Opcodes.DASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;

            case Opcodes.BALOAD:
                if (this.shouldMutate("Decremented (a--) byte array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.DUP_X2);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.ISUB);
                    mv.visitInsn(Opcodes.I2B);
                    mv.visitInsn(Opcodes.BASTORE);
                } else {
                    mv.visitInsn(opcode);
                }
                break;

            case Opcodes.SALOAD:
                if (this.shouldMutate("Decremented (a--) short array field")) {
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(opcode);
                    mv.visitInsn(Opcodes.DUP_X2);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.ISUB);
                    mv.visitInsn(Opcodes.I2S);
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
                if (this.shouldMutate("Decremented (a--) integer field " + name)) {
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
                if (this.shouldMutate("Decremented (a--) float field " + name)) {
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
                if (this.shouldMutate("Decremented (a--) long field " + name)) {
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
                if (this.shouldMutate("Decremented (a--) double field " + name)) {
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
                if (this.shouldMutate("Decremented (a--) byte field " + name)) {
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
                if (this.shouldMutate("Decremented (a--) short field " + name)) {
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

        // GETSTATIC I,F,L,D + B,S
        if (opcode == Opcodes.GETSTATIC) {
            if (desc.equals("I")) {
                if (this.shouldMutate("Decremented (a--) static integer field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.ISUB);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("F")) {
                if (this.shouldMutate("Decremented (a--) static float field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitInsn(Opcodes.FCONST_1);
                    mv.visitInsn(Opcodes.FSUB);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("J")) {
                if (this.shouldMutate("Decremented (a--) static long field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(Opcodes.LCONST_1);
                    mv.visitInsn(Opcodes.LSUB);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("D")) {
                if (this.shouldMutate("Decremented (a--) static double field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP2);
                    mv.visitInsn(Opcodes.DCONST_1);
                    mv.visitInsn(Opcodes.DSUB);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("B")) {
                if (this.shouldMutate("Decremented (a--) static byte field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.ISUB);
                    mv.visitInsn(Opcodes.I2B);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
            if (desc.equals("S")) {
                if (this.shouldMutate("Decremented (a--) static short field " + name)) {
                    mv.visitFieldInsn(opcode, owner, name, desc);
                    mv.visitInsn(Opcodes.DUP);
                    mv.visitInsn(Opcodes.ICONST_1);
                    mv.visitInsn(Opcodes.ISUB);
                    mv.visitInsn(Opcodes.I2S);
                    mv.visitFieldInsn(Opcodes.PUTSTATIC, owner, name, desc);
                    return;
                }
            }
        }
        mv.visitFieldInsn(opcode, owner, name, desc);
    }
}
