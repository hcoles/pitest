package org.pitest.sequence;

public final class Slot<T> {
  public static <T> Slot<T> create(Class<T> clazz) {
    return new Slot<T>();
  }
}
