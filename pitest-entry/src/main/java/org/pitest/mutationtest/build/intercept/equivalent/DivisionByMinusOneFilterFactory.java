package org.pitest.mutationtest.build.intercept.equivalent;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.MathMutator;
import org.pitest.plugin.Feature;

public class DivisionByMinusOneFilterFactory implements MutationInterceptorFactory {

    @Override
    public String description() {
        return "Division by one equivalent mutant filter";
    }

    @Override
    public Feature provides() {
        return Feature.named("FSEQUIVDIV")
                .withOnByDefault(true)
                .withDescription("Filters equivalent mutations of the form x * -1 -> x / -1");
    }

    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        return new DivisionByMinusOneFilter(MathMutator.MATH);
    }

}
