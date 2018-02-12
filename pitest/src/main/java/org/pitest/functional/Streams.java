package org.pitest.functional;

import java.util.stream.Stream;

public class Streams {
  public static <T> Stream<T> fromOptional(java.util.Optional<T> opt) {
    if (opt.isPresent()) {
      return Stream.of(opt.get());
    } else {
      return Stream.empty();
    }
  }
}
