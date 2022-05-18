package org.pitest.sequence;

import java.util.Optional;

public interface Context {

  static Context start() {
    return start(false);
  }

  static Context start(boolean debug) {
    if (debug) {
      return EmptyContext.WITH_DEBUG;
    }
    return EmptyContext.WITHOUT_DEBUG;
  }

  <S> Context store(SlotWrite<S> slot, S value);

  @SuppressWarnings("unchecked")
  <S> Optional<S> retrieve(SlotRead<S> slot);

  default boolean debug() {
    return false;
  }

  default <T> void debug(String msg, T t) {
    if (debug()) {
      System.out.println(msg + " for " + t);
    }
  }
}
