package org.pitest.sequence;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Specialisation of context for single values
 */
final class Context1 implements Context {
    private final boolean debug;
    private final Slot<?> slot;
    private final Object value;

    Context1(Slot<?> slot, Object value, boolean debug) {
        this.slot = slot;
        this.value = value;
        this.debug = debug;
    }

    @Override
    public boolean debug() {
        return debug;
    }

    @Override
    public <S> Context store(SlotWrite<S> slot, S value) {
        Map<Slot,Object> mutatedSlots = new IdentityHashMap<>();
        mutatedSlots.put(this.slot, this.value);
        mutatedSlots.put(slot.slot(), value);
        return new MultiContext(mutatedSlots, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <S> Optional<S> retrieve(SlotRead<S> read) {
        if (read.slot().equals(slot)) {
            return (Optional<S>) Optional.ofNullable(value);
        }
        return Optional.empty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Context1)) {
            return false;
        }
        Context1 context1 = (Context1) o;
        return slot.equals(context1.slot) && Objects.equals(value, context1.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(slot, value);
    }
}
