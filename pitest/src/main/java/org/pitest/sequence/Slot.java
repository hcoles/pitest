package org.pitest.sequence;

public final class Slot<T> {
  public static <T> Slot<T> create(Class<T> clazz) {
    return new Slot<T>();
  }
  
  public SlotWrite<T> write() {
    return new SlotWrite<T>(this);
  }
  
  public SlotRead<T> read() {
    return new SlotRead<T>(this);
  }
}
