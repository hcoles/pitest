package org.pitest.plugin.available;

import org.pitest.mutationtest.build.InterceptorParameters;
import org.pitest.mutationtest.build.MutationInterceptor;
import org.pitest.mutationtest.build.MutationInterceptorFactory;
import org.pitest.plugin.Feature;
import org.pitest.plugin.MissingPlugin;

public class MissingGitFeature implements MutationInterceptorFactory, MissingPlugin {
    @Override
    public MutationInterceptor createInterceptor(InterceptorParameters params) {
        throw new PluginNotInstalledException("Git integration requires the Git plugin to be installed. It's available from https://www.arcmutate.com/");
    }

    @Override
    public Feature provides() {
        return Feature.named("GIT")
                .markMissing()
                .withDescription(description());
    }

    @Override
    public String description() {
        return "Git integration";
    }
}
