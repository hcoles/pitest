package org.pitest.mutationtest.engine.gregor;

import org.pitest.mutationtest.engine.MutationIdentifier;

import java.util.Optional;

public interface MethodMutatorFactoryWithInfo extends MethodMutatorFactory, MutatorInfo {

    @Override
    default String description() {
        return MethodMutatorFactory.super.description();
    }

    @Override
    default Optional<MutantUrl> urlForMutant(MutationIdentifier mutationIdentifier) {
        if (this.isMutatorFor(mutationIdentifier)) {
            return Optional.of(new MutantUrl(UrlType.DOC, url()));
        }
        return Optional.empty();
    }

    String url();
}
