package org.pitest.functional;

import java.util.Collection;
import java.util.stream.Stream;

public class Streams {

  /**
   * Convert to stream with a null check. Added for easy migration
   * from legacy FCollection class.
   *
   * @param c a collection
   * @return a stream
   */
  public static <T> Stream<T> asStream(Collection<T> c) {
    if (c == null) {
      return Stream.empty();
    }
    return c.stream();
  }
}
