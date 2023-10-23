package org.pitest.mutationtest.build.intercept.exclude;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.intercept.groovy.GroovyFilterFactory;
import org.pitest.verifier.interceptors.FactoryVerifier;

public class FirstLineInterceptorFactoryTest {

    FirstLineInterceptorFactory underTest = new FirstLineInterceptorFactory();

    @Test
    public void isOnChain() {
        FactoryVerifier.confirmFactory(underTest)
                .isOnChain();
    }

    @Test
    public void isOffByDefault() {
        FactoryVerifier.confirmFactory(underTest)
                .isOffByDefault();
    }


    @Test
    public void featureIsCalledNoFirstLine() {
        FactoryVerifier.confirmFactory(underTest)
                .featureName().isEqualTo("nofirstline");
    }

    @Test
    public void createsFilters() {
        FactoryVerifier.confirmFactory(underTest)
                .createsInterceptorsOfType(InterceptorType.FILTER);
    }


}