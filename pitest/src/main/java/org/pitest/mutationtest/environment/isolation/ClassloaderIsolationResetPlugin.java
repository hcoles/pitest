package org.pitest.mutationtest.environment.isolation;

import org.pitest.mutationtest.environment.EnvironmentResetPlugin;
import org.pitest.mutationtest.environment.ResetEnvironment;
import org.pitest.plugin.Feature;

public class ClassloaderIsolationResetPlugin implements EnvironmentResetPlugin {
    @Override
    public ResetEnvironment make() {
        return CatchNewClassLoadersTransformer::setMutant;
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
