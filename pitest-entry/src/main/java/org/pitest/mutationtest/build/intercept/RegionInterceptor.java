package org.pitest.mutationtest.build.intercept;

import org.pitest.bytecode.analysis.ClassTree;
import org.pitest.bytecode.analysis.MethodTree;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;

import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Base class to perform donkey work for interceptors that compute excluded regions per method
 */
public abstract class RegionInterceptor implements MutationInterceptor {
    private ClassTree currentClass;
    private Map<MethodTree, List<RegionIndex>> cache;

    @Override
    public InterceptorType type() {
        return InterceptorType.FILTER;
    }

    @Override
    public void begin(ClassTree clazz) {
        currentClass = clazz;
        cache = new IdentityHashMap<>();
    }

    @Override
    public Collection<MutationDetails> intercept(
            Collection<MutationDetails> mutations, Mutater m) {
        return mutations.stream()
                .filter(buildPredicate().negate())
                .collect(Collectors.toList());
    }


    protected Predicate<MutationDetails> buildPredicate() {
        return a -> {
            final int instruction = a.getInstructionIndex();
            final Optional<MethodTree> method = this.currentClass.method(a.getId().getLocation());

            if (!method.isPresent()) {
                return false;
            }

            List<RegionIndex> regions = cache.computeIfAbsent(method.get(), this::computeRegionIndex);
            return regions.stream()
                    .anyMatch(r -> r.start() <= instruction && r.end() >= instruction);
        };
    }

    private List<RegionIndex> computeRegionIndex(MethodTree method) {
        return computeRegions(method).stream()
                .map(r -> new RegionIndex(method.instructions().indexOf(r.start), method.instructions().indexOf(r.end)))
                .collect(Collectors.toList());
    }

    protected abstract List<Region> computeRegions(MethodTree method);

    @Override
    public void end() {
        currentClass = null;
        cache = null;
    }

}
