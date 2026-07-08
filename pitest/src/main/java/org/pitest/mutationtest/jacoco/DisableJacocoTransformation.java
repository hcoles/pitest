package org.pitest.mutationtest.jacoco;

import org.pitest.mutationtest.environment.TransformationPlugin;


import java.lang.instrument.ClassFileTransformer;

/**
 * Jacoco instrumentation can cause subtle issues with coverage tracking.
 * In case the user hasn't disabled it, we prevent the issue by transforming
 * its instrumentation classes to return unmodified bytecode.
 */
public class DisableJacocoTransformation implements TransformationPlugin {
    @Override
    public ClassFileTransformer makeCoverageTransformer() {
        return new DisableJacocoTransformer();
    }

    @Override
    public ClassFileTransformer makeMutationTransformer() {
        return new DisableJacocoTransformer();
    }

    @Override
    public String description() {
        return "Disable JaCoCo";
    }
}
