package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class AssertionsFilterFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "Assertions filter";
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new AssertFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("FASSERT")
                .withOnByDefault(true)
                .withDescription("Filters mutations in compiler generated code for assertions");
    }

}
