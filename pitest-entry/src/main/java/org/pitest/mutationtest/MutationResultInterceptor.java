package org.pitest.mutationtest;

import org.pitest.plugin.ToolClasspathPlugin;

import java.util.Collection;
import java.util.Collections;

/**
 * Allows modification of results before reporting
 */
public interface MutationResultInterceptor extends ToolClasspathPlugin {
    /**
     * Modify results for a class
     * @param results Results
     * @return Results with modifications
     */
    Collection<ClassMutationResults> modify(Collection<ClassMutationResults> results);

    /**
     * Called at end of run to provide any results that could not be determined
     * until all processing was complete
     * @return Collection of results
     */
    default Collection<ClassMutationResults> remaining() {
        return Collections.emptyList();
    }

    @Override
    default String description() {
        return "";
    }

    default int priority() {
      return 10;
    }
}
