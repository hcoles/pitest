package org.pitest.bytecode.analysis;

import org.pitest.functional.F;
import org.pitest.mutationtest.engine.Location;

public class MethodMatchers {
  /**
   * Match a method based on its name, as methods can be overloaded
   * this should be used with caution.
   * @param name the methods name
   * @return true if matched
   */
  public static F<MethodTree, Boolean> named(final String name) {
    return new F<MethodTree, Boolean>() {
      @Override
      public Boolean apply(MethodTree a) {
        return a.rawNode().name.equals(name);
      }
    };
  }

  public static F<MethodTree, Boolean> forLocation(final Location location) {
    return new F<MethodTree, Boolean>() {
      @Override
      public Boolean apply(MethodTree a) {
        return a.asLocation().equals(location);
      }
    };
  }
}
