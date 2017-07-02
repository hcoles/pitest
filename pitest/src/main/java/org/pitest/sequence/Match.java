package org.pitest.sequence;

/**
 * Predicate with additional context.
 *
 * Implemented as abstract class as we're still on Java
 * 6 and don't have default methods
 *
 * @param <T> Type to match
 */
public abstract class Match<T> {

  public abstract boolean test(Context<T> c, T t);

  public static <T> Match<T> always() {
    return new Match<T>() {
      @Override
      public boolean test(Context<T> c, T t) {
        return true;
      }
    };
  }
  
  public static <T> Match<T> never() {
    return new Match<T>() {
      @Override
      public boolean test(Context<T> c, T t) {
        return false;
      }
    };
  }

  public static <T> Match<T> isEqual(final Object targetRef) {
    return new Match<T>() {
      @Override
      public boolean test(Context<T> c, T t) {
        return targetRef.equals(t);
      }
    };
  }
  
  public Match<T> and(final Match<T> other) {
      final Match<T> self = this;
      return new Match<T>() {
        @Override
        public boolean test(Context<T> c, T t) {
          return self.test(c,t) && other.test(c,t);
        }
      };
  }
  
  public Match<T> negate() {
    final Match<T> self = this;
    return new Match<T>() {
      @Override
      public boolean test(Context<T> c, T t) {
        return !self.test(c,t);
      }
    };
  }

  public Match<T> or(final Match<T> other) {
    final Match<T> self = this;
    return new Match<T>() {
      @Override
      public boolean test(Context<T> c, T t) {
        return self.test(c,t) || other.test(c,t);
      }
    };
  }
}
