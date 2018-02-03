package org.pitest.sequence;

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

  boolean test(Context<T> c, T t);

  static <T> Match<T> always() {
    return (c, t) -> true;
  }

  static <T> Match<T> never() {
    return (c, t) -> false;
  }

  static <T> Match<T> isEqual(final Object targetRef) {
    return (c, t) -> targetRef.equals(t);
  }

  default Match<T> and(final Match<T> other) {
      return (c, t) -> this.test(c,t) && other.test(c,t);
  }

  default Match<T> negate() {
    return (c, t) -> !this.test(c,t);
  }

  default Match<T> or(final Match<T> other) {
    return (c, t) -> this.test(c,t) || other.test(c,t);
  }
}
