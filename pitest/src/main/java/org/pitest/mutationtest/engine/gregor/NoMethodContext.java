package org.pitest.mutationtest.engine.gregor;

import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

public class NoMethodContext implements BasicContext {

    private final ClassContext context;

    public NoMethodContext(ClassContext context) {
        this.context = context;
    }

    @Override
    public ClassInfo getClassInfo() {
        return context.getClassInfo();
    }

    @Override
    public void registerMutation(MutationIdentifier id, String description) {
        registerMutation(new MutationDetails(id, context.getFileName(), description,0, 1));
    }

    @Override
    public boolean shouldMutate(MutationIdentifier id) {
        return this.context.shouldMutate(id);
    }

    private void registerMutation(MutationDetails details) {
        this.context.addMutation(details);
    }
}
