package org.pitest.sequence;

import java.util.function.Predicate;

import static org.pitest.sequence.Result.result;

/**
 * Predicate with additional context.
 *
 * Implemented as abstract class as we're still on Java
 * 6 and don't have default methods
 *
 * @param <T> Type to match
 */
@FunctionalInterface
public interface Match<T> {

  Result test(Context c, T t);

  static <T> Match<T> always() {
    return (c, t) -> result(true, c);
  }

  static <T> Match<T> never() {
    return (c, t) -> result(false, c);
  }

  static <T> Match<T> isEqual(final Object targetRef) {
    return (c, t) -> result(targetRef.equals(t), c);
  }

  default Match<T> and(final Match<T> other) {
      return (c, t) -> {
        Result r = this.test(c,t);
        if (!r.result()) {
          return r;
        }
        return other.test(r.context(), t);
      };
  }

  default Match<T> negate() {
    return (c, t) -> {
      Result r = this.test(c,t);
      if (!r.result()) {
        return result(true, r.context());
      }
      return result(false, c);
    };
  }

  default Match<T> or(final Match<T> other) {
    return (c, t) -> {
      Result r = this.test(c,t);
      if (r.result()) {
        return r;
      }
      return other.test(c,t);
    };
  }

  /**
   * Convert to plain predicate with no context
   */
  default Predicate<T> asPredicate() {
    return t -> this.test(Context.start(), t).result();
  }
}
