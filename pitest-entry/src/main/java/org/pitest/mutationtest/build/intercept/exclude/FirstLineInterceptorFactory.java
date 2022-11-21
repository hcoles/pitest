package org.pitest.mutationtest.build.intercept.exclude;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class FirstLineInterceptorFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "Filters mutants with line number <= 1";
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new FirstLineFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("nofirstline")
                .withDescription(description())
                .withOnByDefault(false);
    }

}
