package org.pitest.bytecode.analysis;

import org.pitest.functional.predicate.Predicate;
import org.pitest.mutationtest.engine.Location;

public class MethodMatchers {
  /**
   * Match a method based on its name, as methods can be overloaded
   * this should be used with caution.
   * @param name the methods name
   * @return true if matched
   */
  public static Predicate<MethodTree> named(final String name) {
    return new Predicate<MethodTree>() {
      @Override
      public Boolean apply(MethodTree a) {
        return a.rawNode().name.equals(name);
      }
    };
  }

  public static Predicate<MethodTree> forLocation(final Location location) {
    return new Predicate<MethodTree>() {
      @Override
      public Boolean apply(MethodTree a) {
        return a.asLocation().equals(location);
      }
    };
  }
}
