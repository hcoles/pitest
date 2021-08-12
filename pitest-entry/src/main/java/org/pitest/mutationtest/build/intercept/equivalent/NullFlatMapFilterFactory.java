package org.pitest.mutationtest.build.intercept.equivalent;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class NullFlatMapFilterFactory implements MutationInterceptorFactory {

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new NullFlatMapFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("FNULLSTREAM")
                .withOnByDefault(true)
                .withDescription("Filters equivalent mutations where null is passed to flatmap in place of Stream.empty");
    }

    @Override
    public String description() {
        return "Null stream flatmap filter";
    }
}
