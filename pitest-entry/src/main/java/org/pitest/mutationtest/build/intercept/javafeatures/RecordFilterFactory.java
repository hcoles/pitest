package org.pitest.mutationtest.build.intercept.javafeatures;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;

public class RecordFilterFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "Record junk mutation filter";
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new RecordFilter();
    }

    @Override
    public Feature provides() {
        return Feature.named("FRECORD")
                .withOnByDefault(true)
                .withDescription("Filters mutations in compiler generated record code");
    }

}
