package org.pitest.sequence;

import java.util.Optional;

/**
 * Specialisation of context with no data
 */
enum EmptyContext implements Context {

    WITHOUT_DEBUG(false),
    WITH_DEBUG(true);

    private final boolean debug;

    EmptyContext(boolean debug) {
        this.debug = debug;
    }

    @Override
    public boolean debug() {
        return debug;
    }

    @Override
    public <S> Context store(SlotWrite<S> slot, S value) {
        return new Context1(slot.slot(), value, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> Optional<S> retrieve(SlotRead<S> slot) {
        return Optional.empty();
    }

}
