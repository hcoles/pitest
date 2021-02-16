package org.pitest.functional;

import java.util.stream.Stream;

public class Streams {
  public static <T> Stream<T> fromOptional(java.util.Optional<T> opt) {
    return opt.map(Stream::of).orElseGet(Stream::empty);
  }
}
