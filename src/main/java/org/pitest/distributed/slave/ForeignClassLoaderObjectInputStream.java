package org.pitest.distributed.slave;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

public class ForeignClassLoaderObjectInputStream extends ObjectInputStream {

  private final ClassLoader[] customClassLoaders;

  public ForeignClassLoaderObjectInputStream(final InputStream arg0,
      final ClassLoader... customClassLoaders) throws IOException {
    super(arg0);
    this.customClassLoaders = customClassLoaders;
  }

  @Override
  protected Class<?> resolveClass(final ObjectStreamClass desc)
      throws IOException, ClassNotFoundException {
    final String className = desc.getName();
    try {
      return Class.forName(className);
    } catch (final ClassNotFoundException exc) {
      for (final ClassLoader cl : this.customClassLoaders) {
        try {
          return cl.loadClass(className);
        } catch (final ClassNotFoundException e) {
        }
      }
      throw new ClassNotFoundException(className);
    }
  }

}
