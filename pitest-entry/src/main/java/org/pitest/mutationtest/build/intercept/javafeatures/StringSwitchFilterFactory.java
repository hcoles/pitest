package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class StringSwitchFilterFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "String switch filter";
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new StringSwitchFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("FSTRSWITCH")
                .withOnByDefault(true)
                .withDescription("Filters mutations in compiler generated code for string switch statements");
    }

}