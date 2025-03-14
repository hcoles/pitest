package org.pitest.mutationtest.engine.gregor;

import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.plugin.ToolClasspathPlugin;

import java.util.Optional;

public interface MutatorInfo extends ToolClasspathPlugin {
    Optional<MutantUrl> urlForMutant(MutationIdentifier id);
}
