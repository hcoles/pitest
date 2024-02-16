package org.pitest.mutationtest.build.intercept.defensive;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class UnmodifiableCollectionFactory implements MutationInterceptorFactory {
    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new UnmodifiableCollections();
    }

    @Override
    public Feature provides() {
        return Feature.named("funmodifiablecollection")
                .withOnByDefault(false)
                .withDescription(description());
    }

    @Override
    public String description() {
        return "Filter mutations to defensive wrappers such as unmodifiableCollection on return or field write";
    }
}
