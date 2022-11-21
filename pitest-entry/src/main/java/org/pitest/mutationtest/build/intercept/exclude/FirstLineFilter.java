package org.pitest.mutationtest.build.intercept.exclude;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.stream.Collectors;

class FirstLineFilter implements MutationInterceptor {
    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {

    }

    @Override
    public Collection<MutationDetails> intercept(Collection<MutationDetails> mutations, Mutater unused) {
        return mutations.stream()
                .filter(m -> m.getLineNumber() > 1)
                .collect(Collectors.toList());
    }

    @Override
    public void end() {

    }
}
