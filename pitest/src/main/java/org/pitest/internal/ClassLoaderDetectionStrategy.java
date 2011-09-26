package org.pitest.internal;

public interface ClassLoaderDetectionStrategy {

  public boolean fromDifferentLoader(Class<?> clazz, ClassLoader loader);
}
