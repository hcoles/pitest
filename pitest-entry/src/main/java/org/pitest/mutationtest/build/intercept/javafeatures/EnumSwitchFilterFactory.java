package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class EnumSwitchFilterFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "Enum switch filter";
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new EnumSwitchFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("FESWITCH")
                .withOnByDefault(true)
                .withDescription("Filters mutations in switch statements on enums");
    }

}
