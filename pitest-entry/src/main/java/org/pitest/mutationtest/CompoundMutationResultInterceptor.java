package org.pitest.mutationtest;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundMutationResultInterceptor implements MutationResultInterceptor {

    private final List<MutationResultInterceptor> interceptors;

    public CompoundMutationResultInterceptor(List<MutationResultInterceptor> interceptors) {
        this.interceptors = interceptors.stream()
                .sorted(Comparator.comparing(MutationResultInterceptor::priority))
                .collect(Collectors.toList());
    }

    public CompoundMutationResultInterceptor add(MutationResultInterceptor extra) {
        interceptors.add(0, extra);
        return this;
    }

    @Override
    public Collection<ClassMutationResults> modify(Collection<ClassMutationResults> in) {
        Collection<ClassMutationResults> result = in;
        for (MutationResultInterceptor each : interceptors) {
            result = each.modify(result);
        }
        return result;
    }

    @Override
    public Collection<ClassMutationResults> remaining() {
        return interceptors.stream()
                .flatMap(i -> i.remaining().stream())
                .collect(Collectors.toList());
    }

    @Override
    public String description() {
        return "Composite interceptor";
    }
}
