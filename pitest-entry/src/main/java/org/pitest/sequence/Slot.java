package org.pitest.sequence;

import java.util.List;
import java.util.Set;

public final class Slot<T> {
    public static <T> Slot<T> create(Class<T> clazz) {
        return new Slot<>();
    }

    public static <T> Slot<List<T>> createList(Class<T> clazz) {
        return new Slot<>();
    }

    public static <T> Slot<Set<T>> createSet(Class<T> clazz) {
        return new Slot<>();
    }

    public SlotWrite<T> write() {
        return new SlotWrite<>(this);
    }

    public SlotRead<T> read() {
        return new SlotRead<>(this);
    }
}
