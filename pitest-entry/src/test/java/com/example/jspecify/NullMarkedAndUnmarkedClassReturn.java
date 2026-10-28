package com.example.jspecify;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.NullUnmarked;

import java.util.concurrent.Callable;

/**
 * Carries both annotations, which the spec treats as neither being present, so
 * the null-marked package decides.
 */
@NullUnmarked
@NullMarked
public class NullMarkedAndUnmarkedClassReturn implements Callable<Object> {

  @Override
  public Object call() {
    return new Object();
  }
}
