package com.example.jspecify;

import java.util.concurrent.Callable;

/**
 * Returns an object from a package declared {@code @NullMarked}, so the return
 * type is non-null by default.
 */
public class NullMarkedPackageReturn implements Callable<Object> {

  @Override
  public Object call() {
    return new Object();
  }
}
