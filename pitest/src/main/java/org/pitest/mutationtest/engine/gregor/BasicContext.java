package org.pitest.mutationtest.engine.gregor;

import org.pitest.mutationtest.engine.MutationIdentifier;

public interface BasicContext {

    ClassInfo getClassInfo();

    void registerMutation(MutationIdentifier id, String description);

    boolean shouldMutate(MutationIdentifier newId);
}
