package org.pitest.mutationtest;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class CompoundMutationResultInterceptor implements MutationResultInterceptor {

    private final List<MutationResultInterceptor> interceptors;

    public CompoundMutationResultInterceptor(List<MutationResultInterceptor> interceptors) {
        this.interceptors = interceptors;
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
