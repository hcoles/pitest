package org.pitest.sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import java.util.Optional;

public class Context<T> {

  private final boolean debug;
  private final Map<Slot<?>, Object> slots;
  private final List<T> sequence;
  private int position;

  Context(Map<Slot<?>, Object> slots, List<T> sequence, int position, boolean debug) {
    this.slots = slots;
    this.sequence = sequence;
    this.position = position;
    this.debug = debug;
  }

  public static <T> Context<T> start(List<T> sequence) {
    return start(sequence, false);
  }

  public static <T> Context<T> start(List<T> sequence, boolean debug) {
    return new Context<>(new HashMap<Slot<?>, Object>(), sequence, -1, debug);
  }

  public <S> boolean store(SlotWrite<S> slot, S value) {
    this.slots.put(slot.slot(), value);
    return true;
  }

  @SuppressWarnings("unchecked")
  public <S> Optional<S> retrieve(SlotRead<S> slot) {
    return (Optional<S>) Optional.ofNullable(this.slots.get(slot.slot()));
  }


  void moveForward() {
    this.position = this.position + 1;
  }

  public int position() {
    return this.position;
  }

  public void debug(String msg) {
    if (this.debug) {
      System.out.println(msg + " at " + this.position + " for " + this.sequence.get(this.position));
    }
  }

}
