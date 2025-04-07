package org.pitest.mutationtest.config;

import org.assertj.core.api.AbstractStringAssert;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public class ConfigUpdaterVerifier {
    private final ConfigurationUpdater factory;

    public ConfigUpdaterVerifier(ConfigurationUpdater factory) {
        this.factory = factory;
    }

    public static ConfigUpdaterVerifier confirmFactory(ConfigurationUpdater factory) {
        return new ConfigUpdaterVerifier(factory);
    }

    public void isOnChain() {
        factoryIsOnChain(factory.getClass());
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
        List<Class<?>> allInterceptors = PluginServices.makeForContextLoader().findConfigurationUpdaters().stream()
                .map(ConfigurationUpdater::getClass)
                .collect(Collectors.toList());

        assertThat(allInterceptors).contains(factory);
    }

}
