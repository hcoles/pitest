package org.pitest.mutationtest.environment.isolation;

import org.pitest.mutationtest.environment.TransformationPlugin;

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
}
