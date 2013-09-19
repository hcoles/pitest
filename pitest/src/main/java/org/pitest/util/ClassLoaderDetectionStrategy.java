package org.pitest.util;

public interface ClassLoaderDetectionStrategy {

  public boolean fromDifferentLoader(Class<?> clazz, ClassLoader loader);
}
