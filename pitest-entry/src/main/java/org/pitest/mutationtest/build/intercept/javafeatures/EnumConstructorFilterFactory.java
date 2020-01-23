package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class EnumConstructorFilterFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "Enum constructor filter";
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new EnumConstructorFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("FENUM")
                .withOnByDefault(true)
                .withDescription("Filters mutations in enum constructors");
    }

}

