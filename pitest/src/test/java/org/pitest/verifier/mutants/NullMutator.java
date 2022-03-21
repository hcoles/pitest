package org.pitest.verifier.mutants;

import org.objectweb.asm.MethodVisitor;
import org.pitest.bytecode.ASMVersion;
import org.pitest.mutationtest.engine.gregor.MethodInfo;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;
import org.pitest.mutationtest.engine.gregor.MutationContext;

/**
 * Non mutating mutator. We add this to the verifier as a (not very robust) check
 * that the mutators are providing a unique id. If they returned an empty string
 * as their id this mutator might be picked up instead (depending on order).
 */
public class NullMutator implements MethodMutatorFactory {
    @Override
    public MethodVisitor create(MutationContext mutationContext, MethodInfo methodInfo, MethodVisitor methodVisitor) {
        return new MethodVisitor(ASMVersion.ASM_VERSION) {};
    }

    @Override
    public String getGloballyUniqueId() {
        return "";
    }

    @Override
    public String getName() {
        return "";
    }
}
