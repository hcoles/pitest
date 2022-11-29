package org.pitest.mutationtest.build.intercept.groovy;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.intercept.exclude.FirstLineInterceptorFactory;
import org.pitest.mutationtest.build.intercept.staticinitializers.StaticInitializerInterceptorFactory;
import org.pitest.mutationtest.engine.gregor.mutators.NullMutateEverything;
import org.pitest.verifier.interceptors.FactoryVerifier;
import org.pitest.verifier.interceptors.InterceptorVerifier;
import org.pitest.verifier.interceptors.VerifierStart;

import static org.assertj.core.api.Assertions.assertThat;

public class GroovyFilterFactoryTest {
    @Test
    public void isOnChain() {
        FactoryVerifier.confirmFactory(new FirstLineInterceptorFactory())
                .isOnChain();
    }

    @Test
    public void isOffByDefault() {
        FactoryVerifier.confirmFactory(new FirstLineInterceptorFactory())
                .isOffByDefault();
    }


    @Test
    public void featureIsCalledNoFirstLine() {
        FactoryVerifier.confirmFactory(new FirstLineInterceptorFactory())
                .featureName().isEqualTo("nofirstline");
    }

    @Test
    public void createsFilters() {
        FactoryVerifier.confirmFactory(new FirstLineInterceptorFactory())
                .createsInterceptorsOfType(InterceptorType.FILTER);
    }
}