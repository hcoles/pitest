package org.pitest.classpath;

public interface ClassLoaderDetectionStrategy {

  public boolean fromDifferentLoader(Class<?> clazz, ClassLoader loader);
}
