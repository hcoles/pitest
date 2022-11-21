package org.pitest.mutationtest.build.intercept.exclude;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.verifier.interceptors.FactoryVerifier;

public class FirstLineInterceptorFactoryTest {

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