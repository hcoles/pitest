package org.pitest.mutationtest.environment;

import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.plugin.Feature;
import org.pitest.plugin.ProvidesFeature;

public interface EnvironmentResetPlugin extends ClientClasspathPlugin, ProvidesFeature {

    ResetEnvironment make();

    @Override
    // provide default feature for backwards compatibility
    default Feature provides() {
        return Feature.named("_internal")
                .withOnByDefault(true)
                .asInternalFeature();
    }
}
