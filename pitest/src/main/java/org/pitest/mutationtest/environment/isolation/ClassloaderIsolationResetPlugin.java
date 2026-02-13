package org.pitest.mutationtest.environment.isolation;

import org.pitest.mutationtest.engine.Mutant;
import org.pitest.mutationtest.environment.EnvironmentResetPlugin;
import org.pitest.mutationtest.environment.ResetArguments;
import org.pitest.mutationtest.environment.ResetEnvironment;
import org.pitest.plugin.Feature;

public class ClassloaderIsolationResetPlugin implements EnvironmentResetPlugin {
    @Override
    public ResetEnvironment make(ResetArguments unused) {
        return new CatchNewClassLoadersReset();
    }

    @Override
    public String description() {
        return "Restore classes to unmutated version in other classloaders";
    }

    @Override
    public Feature provides() {
        return Feature.named("isolate_classloaders")
                .withOnByDefault(true)
                .withDescription(description());
    }
}

class CatchNewClassLoadersReset implements ResetEnvironment {
    @Override
    public void resetFor(Mutant mutatedClass, ClassLoader unused) {
        CatchNewClassLoadersTransformer.setMutant(mutatedClass);
    }

    @Override
    public void finishFor(Mutant mutatedClass, ClassLoader loader) {
        CatchNewClassLoadersTransformer.reset();
    }
}
