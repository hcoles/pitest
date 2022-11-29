package org.pitest.mutationtest.build.intercept.groovy;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class GroovyFilterFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "Groovy junk mutations filter";
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new GroovyFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("FGROOVY")
                .withDescription("Filters out junk mutations in groovy code")
                .withOnByDefault(true);
    }

}
