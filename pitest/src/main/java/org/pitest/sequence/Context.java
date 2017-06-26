package org.pitest.sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.pitest.functional.Option;

public class Context<T> {
  private final Map<Slot<?>, Object> slots;
  private final List<T> sequence;
  
  private final int position;

  Context(Map<Slot<?>, Object> slots, List<T> sequence, int position) {
    this.slots = slots;
    this.sequence = sequence;
    this.position = position;
  }

  public static <T> Context<T> start( List<T> sequence) {
    return new Context<T>(new HashMap<Slot<?>, Object>(), sequence, -1);
  }
  
  public <S> void store(SlotWrite<S> slot, S value) {
    slots.put(slot.slot(), value);
  }
  
  @SuppressWarnings("unchecked")
  public <S> Option<S> retrieve(SlotRead<S> slot) {
    return (Option<S>) Option.some(slots.get(slot.slot()));
  }
  
  
  Context<T> moveForward() {
    return new Context<T>(slots, sequence, position + 1);
  }

  public boolean lookAhead(Match<T> next) {
    return position + 1 < sequence.size() && next.test(moveForward(), sequence.get(position + 1));
  }
  
}
