package org.pitest.util;

public interface ClassLoaderDetectionStrategy {

  boolean fromDifferentLoader(Class<?> clazz, ClassLoader loader);
}
