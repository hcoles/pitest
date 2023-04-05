package org.pitest.mutationtest.environment;

import org.pitest.mutationtest.engine.Mutant;

public interface ResetEnvironment {
    void resetFor(Mutant mutatedClass);
}
