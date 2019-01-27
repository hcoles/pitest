package org.pitest.mutationtest.engine.gregor.mutators.rv;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.pitest.mutationtest.engine.gregor.AbstractInsnMutator;
import org.pitest.mutationtest.engine.gregor.InsnSubstitution;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;
import org.pitest.mutationtest.engine.gregor.ZeroOperandMutation;

/**
 * Replaces bitwise "and" and "or" with each other
 */
public enum OBBN1Mutator implements MethodMutatorFactory {

    OBBN_1_MUTATOR;

    public MethodVisitor create(final MutationContext context,
                                final MethodInfo methodInfo, final MethodVisitor methodVisitor) {
        return new OBBN1MethodVisitor(this, methodInfo, context, methodVisitor);
    }

    public String getGloballyUniqueId() {
        return this.getClass().getName();
    }

    public String getName() {
        return name();
    }

}

class OBBN1MethodVisitor extends AbstractInsnMutator {

    OBBN1MethodVisitor(final MethodMutatorFactory factory,
                       final MethodInfo methodInfo, final MutationContext context,
                       final MethodVisitor writer) {
        super(factory, methodInfo, context, writer);
    }

    private static final Map<Integer, ZeroOperandMutation> MUTATIONS = new HashMap<>();

    static {
        // integers
        MUTATIONS.put(Opcodes.IAND, new InsnSubstitution(Opcodes.IOR,
                "Replaced integer and with or"));
        MUTATIONS.put(Opcodes.IOR, new InsnSubstitution(Opcodes.IAND,
                "Replaced integer or with and"));

        // longs
        MUTATIONS.put(Opcodes.LAND, new InsnSubstitution(Opcodes.LOR,
                "Replaced long and with or"));
        MUTATIONS.put(Opcodes.LOR, new InsnSubstitution(Opcodes.LAND,
                "Replaced long or with and"));
    }

    @Override
    protected Map<Integer, ZeroOperandMutation> getMutations() {
        return MUTATIONS;
    }

}