package org.pitest.sequence;

import org.pitest.sequence.SequenceQuery.Literal;
import org.pitest.sequence.SequenceQuery.Repeat;

/**
 * Start point for building sequence queries
 *
 * @param <T> type of sequence
 */
public class QueryStart<T> {

  public static <T> SequenceQuery<T> match(Match<T> p) {
    return new SequenceQuery<>(new Literal<>(p));
  }

  public static <T> SequenceQuery<T> any(Class<T> clazz) {
    final Match<T> p = Match.always();
    return new SequenceQuery<>(new Repeat<>(new Literal<>(p)));
  }

}
