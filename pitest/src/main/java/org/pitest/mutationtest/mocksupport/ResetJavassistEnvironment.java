package org.pitest.mutationtest.mocksupport;

import org.pitest.mutationtest.environment.EnvironmentResetPlugin;
import org.pitest.mutationtest.environment.ResetEnvironment;

public class ResetJavassistEnvironment implements EnvironmentResetPlugin {
    @Override
    public ResetEnvironment make() {
        return JavassistInterceptor::setMutant;
    }

    @Override
    public String description() {
        return "Reset environment for javassist";
    }
}
