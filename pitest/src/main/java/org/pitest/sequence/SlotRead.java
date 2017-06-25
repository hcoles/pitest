package org.pitest.sequence;

public class SlotRead<T> {
  private final Slot<T> slot;

  public SlotRead(Slot<T> slot) {
    this.slot = slot;
  }

  Slot<T> slot() {
    return slot;
  }
}

