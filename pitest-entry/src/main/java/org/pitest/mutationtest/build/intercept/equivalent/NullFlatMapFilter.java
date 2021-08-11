package org.pitest.mutationtest.build.intercept.equivalent;

import org.objectweb.asm.Opcodes;
import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.classinfo.ClassName;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NullFlatMapFilter implements MutationInterceptor {
    private ClassTree currentClass;

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        currentClass = clazz;
    }

    @Override
    public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater unused) {
        return mutations.stream()
                .filter(m -> !this.mutatesStreamEmpty(m))
                .collect(Collectors.toList());
    }

    private boolean mutatesStreamEmpty(MutationDetails mutationDetails) {
        MethodTree mutated = currentClass.method(mutationDetails.getId().getLocation()).get();
        if (!mutated.isPrivate() || !mutated.returns(ClassName.fromClass(Stream.class))) {
            return false;
        }

        if (mutated.instruction(mutationDetails.getInstructionIndex()).getOpcode() != Opcodes.ARETURN) {
            return false;
        }

        return true;
    }

    @Override
    public void end() {
        currentClass = null;
    }
}
