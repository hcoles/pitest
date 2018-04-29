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

public enum OperandReplacement implements MethodMutatorFactory {

    OPERAND_REPLACEMENT_MUTATOR;

    @Override
    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new OperandReplacementMethodVisitor(this, methodInfo, context, methodVisitor);
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

class OperandReplacementMethodVisitor extends AbstractInsnMutator {

    OperandReplacementMethodVisitor(final MethodMutatorFactory factory,
                     final MethodInfo methodInfo, final MutationContext context,
                     final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }

    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<Integer, ZeroOperandMutation>();
    private static final String                            MESSAGE   = "Removed second operand (AOD)";

    static {


        MUTATIONS.put(Opcodes.IADD, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.ISUB, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.IMUL, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.IDIV, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.FADD, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.FMUL, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.FDIV, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.IREM, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.FREM, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.FSUB, new InsnSubstitution(Opcodes.POP, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.DMUL, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.LMUL, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.DDIV, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.LDIV, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.DREM, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.LREM, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.DADD, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.LADD, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.DSUB, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));
        MUTATIONS.put(Opcodes.LSUB, new InsnSubstitution(Opcodes.POP2, "Exp Replaced with First Operand"));

    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }

}