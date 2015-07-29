/*
 * Copyright 2010 Henry Coles
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package org.pitest.mutationtest.execute;

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.pitest.classpath.ClassPath;

public class DefaultPITClassloader extends ClassLoader {

  private final ClassPath classPath;

  public DefaultPITClassloader(final ClassPath cp, final ClassLoader parent) {
    super(parent);
    this.classPath = cp;
  }

  @Override
  protected Class<?> findClass(final String name) throws ClassNotFoundException {

    try {
      final byte[] b = this.classPath.getClassData(name);
      if (b == null) {
        throw new ClassNotFoundException(name);
      }
      definePackage(name);
      return defineClass(name, b);
    } catch (final IOException exception) {
      throw new ClassNotFoundException(name, exception);
    }
  }

  private void definePackage(final String name) {
    final int i = name.lastIndexOf('.');
    if (i != -1) {
      final String pkgname = name.substring(0, i);
      if (this.getPackage(pkgname) == null) {
        definePackage(pkgname, null, null, null, null, null, null, null);
      }
    }

  }

  protected Class<?> defineClass(final String name, final byte[] b) {
    return defineClass(name, b, 0, b.length);
  }

  @Override
  protected URL findResource(final String name) {
    return this.classPath.findResource(name);
  }

  @Override
  protected Enumeration<URL> findResources(final String name) {
    return new Enumeration<URL>() {
      private URL element = findResource(name);

      @Override
      public boolean hasMoreElements() {
        return this.element != null;
      }

      @Override
      public URL nextElement() {
        if (this.element != null) {
          final URL next = this.element;
          this.element = null;
          return next;
        }
        throw new NoSuchElementException();
      }
    };
  }

}