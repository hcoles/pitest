package org.pitest.sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.functional.Option;

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
    return new Context<T>(new HashMap<Slot<?>, Object>(), sequence, -1, debug);
  }
  
  public <S> boolean store(SlotWrite<S> slot, S value) {
    slots.put(slot.slot(), value);
    return true;
  }
  
  @SuppressWarnings("unchecked")
  public <S> Option<S> retrieve(SlotRead<S> slot) {
    return (Option<S>) Option.some(slots.get(slot.slot()));
  }
  
  
  void moveForward() {
    position = position + 1;
  }

  public int position() {
    return position;
  }

  public void debug(String msg) {
    if (debug) {
      System.out.println(msg + " at " + position + " for " + sequence.get(position));
    }
  }
  
}
