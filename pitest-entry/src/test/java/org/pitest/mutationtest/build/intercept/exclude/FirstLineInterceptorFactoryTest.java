package org.pitest.mutationtest.build.intercept.exclude;

import org.junit.Test;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.intercept.groovy.GroovyFilterFactory;
import org.pitest.verifier.interceptors.FactoryVerifier;

public class FirstLineInterceptorFactoryTest {

    GroovyFilterFactory underTest = new GroovyFilterFactory();

    @Test
    public void isOnChain() {
        FactoryVerifier.confirmFactory(underTest)
                .isOnChain();
    }

    @Test
    public void isOnByDefault() {
        FactoryVerifier.confirmFactory(underTest)
                .isOnByDefault();
    }


    @Test
    public void featureIsCalledFGroovy() {
        FactoryVerifier.confirmFactory(underTest)
                .featureName().isEqualTo("fgroovy");
    }

    @Test
    public void createsFilters() {
        FactoryVerifier.confirmFactory(underTest)
                .createsInterceptorsOfType(InterceptorType.FILTER);
    }


}