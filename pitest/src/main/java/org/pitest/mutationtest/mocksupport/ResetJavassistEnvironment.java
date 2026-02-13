package org.pitest.mutationtest.mocksupport;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.environment.EnvironmentResetPlugin;
import org.pitest.mutationtest.environment.ResetArguments;
import org.pitest.mutationtest.environment.ResetEnvironment;
import org.pitest.plugin.Feature;

public class ResetJavassistEnvironment implements EnvironmentResetPlugin {
    @Override
    public ResetEnvironment make(ResetArguments unused) {
        return new JavassistReset();
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

class JavassistReset implements ResetEnvironment {
    @Override
    public void resetFor(Mutant mutatedClass, ClassLoader unused) {
        JavassistInterceptor.setMutant(mutatedClass);
    }

    @Override
    public void finishFor(Mutant mutatedClass, ClassLoader loader) {
        JavassistInterceptor.reset();
    }
}
