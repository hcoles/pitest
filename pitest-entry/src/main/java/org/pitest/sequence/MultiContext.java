package org.pitest.sequence;

import java.util.IdentityHashMap;

import java.util.Map;
import java.util.Optional;

/**
 * Specialisation of context for unlimited values
 */
final class MultiContext implements Context {

    private final boolean debug;
    private final Map<Slot,Object> slots;

    MultiContext(Map<Slot,Object> slots, boolean debug) {
        this.slots = slots;
        this.debug = debug;
    }

    @Override
    public boolean debug() {
        return debug;
    }

    @Override
    public <S> Context store(SlotWrite<S> slot, S value) {
        Map<Slot,Object> mutatedSlots = new IdentityHashMap<>(slots);
        mutatedSlots.put(slot.slot(), value);
        return new MultiContext(mutatedSlots, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> Optional<S> retrieve(SlotRead<S> slot) {
        return Optional.ofNullable((S)slots.get(slot.slot()));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MultiContext)) {
            return false;
        }
        MultiContext context = (MultiContext) o;
        return slots.equals(context.slots);
    }

    @Override
    public int hashCode() {
        return slots.hashCode();
    }
}
