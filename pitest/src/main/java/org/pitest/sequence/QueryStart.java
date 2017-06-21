package org.pitest.sequence;

import org.pitest.functional.predicate.Predicate;
import org.pitest.sequence.SequenceQuery.Literal;
import org.pitest.sequence.SequenceQuery.Repeat;

public class QueryStart<T> {
    
  public static <T> SequenceQuery<T> match(Predicate<T> p) {
    return new SequenceQuery<T>(new Literal<T>(p));
  }
  
  public static <T> SequenceQuery<T> any(Class<T> clazz) {
    Predicate<T> p = any();
    return new SequenceQuery<T>(new Repeat<T>(new Literal<T>(p)));
  }
  
  private static <T> Predicate<T> any() {
    return new Predicate<T>() {
      @Override
      public Boolean apply(T a) {
        return true;
      }   
    };
  }
}
