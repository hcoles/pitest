package org.pitest.verifier.interceptors;

import org.assertj.core.api.AbstractStringAssert;
import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.InterceptorType;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class FactoryVerifier {

    private final MutationInterceptorFactory factory;
    private final ReportOptions data;

    public FactoryVerifier(MutationInterceptorFactory factory) {
        this(factory, emptyOptions());
    }

    public FactoryVerifier(MutationInterceptorFactory factory, ReportOptions data) {
        this.factory = factory;
        this.data = data;
    }

    public static FactoryVerifier confirmFactory(MutationInterceptorFactory factory) {
        return new FactoryVerifier(factory);
    }

    public void isOnChain() {
        factoryIsOnChain(factory.getClass());
    }

    public FactoryVerifier withData(ReportOptions data) {
        return new FactoryVerifier(factory, data);
    }

    public void createsInterceptorsOfType(InterceptorType type) {
        assertThat(factory.createInterceptor(emptyParams(data)).type()).isEqualTo(type);
    }

    public void isOnByDefault() {
        assertThat(factory.provides().isOnByDefault()).isTrue();
    }

    public void isOffByDefault() {
        assertThat(factory.provides().isOnByDefault()).isFalse();
    }

    public AbstractStringAssert<?> featureName() {
        return assertThat(factory.provides().name());
    }

    private static void factoryIsOnChain(Class<?> factory) {
        List<Class<?>> allInterceptors = PluginServices.makeForContextLoader().findInterceptors().stream()
                .map(MutationInterceptorFactory::getClass)
                .collect(Collectors.toList());

        assertThat(allInterceptors).contains(factory);
    }

    public static InterceptorParameters emptyParams(ReportOptions data) {
        return new InterceptorParameters(null, data, null, null, null);
    }

    public static ReportOptions emptyOptions() {
        ReportOptions data = new ReportOptions();
        data.setSourceDirs(Collections.emptyList());
        data.setReportDir("");
        return data;
    }

}