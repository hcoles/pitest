package org.pitest.mutationtest.environment;

import org.pitest.plugin.ClientClasspathPlugin;
import org.pitest.plugin.Feature;
import org.pitest.plugin.ProvidesFeature;

import java.lang.instrument.ClassFileTransformer;

public interface TransformationPlugin extends ClientClasspathPlugin, ProvidesFeature {

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

    @Override
    // provide default feature for backwards compatibility
    default Feature provides() {
        return Feature.named("_internal")
                .withOnByDefault(true)
                .asInternalFeature();
    }

}
