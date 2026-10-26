package org.pitest.mutationtest.build.intercept.jspecify;

import java.util.concurrent.Callable;

/**
 * Opts out of the {@code @NullMarked} default declared by the package, so
 * returning null cannot be ruled out and the mutator must stay enabled.
 */
@NullUnmarked
public class NullUnmarkedClassReturn implements Callable<Object> {

  @Override
  public Object call() {
    return new Object();
  }
}
