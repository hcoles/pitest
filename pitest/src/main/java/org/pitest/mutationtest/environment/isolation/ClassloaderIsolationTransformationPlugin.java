package org.pitest.mutationtest.environment.isolation;

import org.pitest.mutationtest.environment.TransformationPlugin;
import org.pitest.plugin.Feature;

import java.lang.instrument.ClassFileTransformer;

public class ClassloaderIsolationTransformationPlugin implements TransformationPlugin {
    @Override
    public String description() {
        return "Restore classes to unmutated version in other classloaders";
    }

    @Override
    public ClassFileTransformer makeMutationTransformer() {
        return new CatchNewClassLoadersTransformer();
    }

    @Override
    public Feature provides() {
        return Feature.named("isolate_classloaders")
                .withOnByDefault(true)
                .withDescription(description());
    }
}
