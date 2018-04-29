package org.pitest.mutationtest.engine.gregor.mutators.custom;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

import java.util.HashMap;
import java.util.Map;

public enum OperandReplacement1 implements MethodMutatorFactory {

    OPERAND_REPLACEMENT_MUTATOR1;

    @Override
    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new OperandReplacementMethodVisitor1(this, methodInfo, context, methodVisitor);
    }

    @Override
    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    @Override
    public String getName() {
        return name();
    }

}

class OperandReplacementMethodVisitor1 extends AbstractInsnMutator {

    OperandReplacementMethodVisitor1(final MethodMutatorFactory factory,
                                     final MethodInfo methodInfo, final MutationContext context,
                                     final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }

    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<Integer, ZeroOperandMutation>();

    static {


        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.SWAP, Opcodes.POP, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2, "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2,  "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2,  "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2,  "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2,  "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2,  "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2,  "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2,  "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2,  "Expression Replaced with Second Operand"));
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.DUP2, Opcodes.POP2,  "Expression Replaced with Second Operand"));

    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }

}