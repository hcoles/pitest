package org.pitest.mutationtest.build.intercept;

import org.pitest.mutationtest.engine.Mutater;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.gregor.MethodMutatorFactory;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public abstract class MutatorSpecificInterceptor extends RegionInterceptor {
    private final List<MethodMutatorFactory> mutators;

    protected MutatorSpecificInterceptor(List<MethodMutatorFactory> mutators) {
        this.mutators = mutators;
    }

    @Override
    public Collection<MutationDetails> intercept(
            Collection<MutationDetails> mutations, Mutater unused) {
        return mutations.stream()
                .filter(forRelevantMutator().negate().or(buildPredicate().negate()))
                .collect(Collectors.toList());
    }

    private Predicate<MutationDetails> forRelevantMutator() {
        return md -> mutators.stream().anyMatch(m -> m.isMutatorFor(md.getId()));
    }
}
