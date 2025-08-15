package org.pitest.mutationtest.mocksupport;

import org.pitest.mutationtest.environment.EnvironmentResetPlugin;
import org.pitest.mutationtest.environment.ResetEnvironment;
import org.pitest.plugin.Feature;

public class ResetJavassistEnvironment implements EnvironmentResetPlugin {
    @Override
    public ResetEnvironment make() {
        return JavassistInterceptor::setMutant;
    }

    @Override
    public Feature provides() {
        return Feature.named("javassist")
                .withOnByDefault(true)
                .withDescription(description());
    }

    @Override
    public String description() {
        return "Reset environment for javassist";
    }
}
