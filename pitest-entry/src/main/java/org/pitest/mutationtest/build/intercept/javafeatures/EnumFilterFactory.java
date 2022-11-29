package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class EnumFilterFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "Enum junk filter";
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new EnumFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("FENUM")
                .withOnByDefault(true)
                .withDescription("Filters junk mutations in enums");
    }

}

