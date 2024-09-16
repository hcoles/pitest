package org.pitest.mutationtest.environment;

import org.pitest.plugin.ClientClasspathPlugin;

import java.lang.instrument.ClassFileTransformer;

public interface TransformationPlugin extends ClientClasspathPlugin {

    @Deprecated
    default ClassFileTransformer makeTransformer() {
        return makeMutationTransformer();
    }

    default ClassFileTransformer makeCoverageTransformer() {
        return null;
    }

    default ClassFileTransformer makeMutationTransformer() {
        return null;
    }

}
