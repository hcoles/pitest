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
    return new SequenceQuery<T>(new Literal<T>(p));
  }
  
  public static <T> SequenceQuery<T> any(Class<T> clazz) {
    Match<T> p = Match.always();
    return new SequenceQuery<T>(new Repeat<T>(new Literal<T>(p)));
  }
  
}
