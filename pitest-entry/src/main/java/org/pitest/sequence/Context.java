package org.pitest.sequence;

import java.util.IdentityHashMap;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class Context {

  private final boolean debug;
  private final Map<Slot,Object> slots;

  Context(Map<Slot,Object> slots, boolean debug) {
    this.slots = slots;
    this.debug = debug;
  }

  public static Context start() {
    return start(false);
  }

  public static Context start(boolean debug) {
    return new Context(new IdentityHashMap<>(), debug);
  }

  public <S> Context store(SlotWrite<S> slot, S value) {
    Map<Slot,Object> mutatedSlots = new IdentityHashMap<>(slots);
    mutatedSlots.put(slot.slot(), value);
    return new Context(mutatedSlots, debug);
  }

  @SuppressWarnings("unchecked")
  public <S> Optional<S> retrieve(SlotRead<S> slot) {
    return Optional.ofNullable((S)slots.get(slot.slot()));
  }

  public <T> void debug(String msg, T t) {
    if (this.debug) {
      System.out.println(msg + " for " + t);
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Context context = (Context) o;
    return debug == context.debug && Objects.equals(slots, context.slots);
  }

  @Override
  public int hashCode() {
    return slots.hashCode();
  }
}
