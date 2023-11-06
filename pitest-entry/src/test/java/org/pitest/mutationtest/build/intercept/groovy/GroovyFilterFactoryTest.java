package org.pitest.mutationtest.build.intercept.groovy;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.verifier.interceptors.FactoryVerifier;

public class GroovyFilterFactoryTest {
    @Test
    public void isOnChain() {
        FactoryVerifier.confirmFactory(new GroovyFilterFactory())
                .isOnChain();
    }

    @Test
    public void isOnByDefault() {
        FactoryVerifier.confirmFactory(new GroovyFilterFactory())
                .isOnByDefault();
    }


    @Test
    public void featureIsCalledFGroovy() {
        FactoryVerifier.confirmFactory(new GroovyFilterFactory())
                .featureName().isEqualTo("fgroovy");
    }

    @Test
    public void createsFilters() {
        FactoryVerifier.confirmFactory(new GroovyFilterFactory())
                .createsInterceptorsOfType(InterceptorType.FILTER);
    }
}