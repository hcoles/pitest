package org.pitest.sequence;

public class SlotWrite <T> {
  private final Slot<T> slot;

  public SlotWrite(Slot<T> slot) {
    this.slot = slot;
  }

  Slot<T> slot() {
    return this.slot;
  }
}
