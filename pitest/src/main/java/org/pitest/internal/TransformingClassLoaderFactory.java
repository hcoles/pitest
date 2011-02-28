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
package org.pitest.internal;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.pitest.extension.ClassLoaderFactory;
import org.pitest.extension.IsolationStrategy;
import org.pitest.extension.Transformation;
import org.pitest.internal.classloader.TransformingClassLoader;

public class TransformingClassLoaderFactory implements ClassLoaderFactory {

  private final Transformation           transformation;
  private final IsolationStrategy        s;
  private final ClassPath                classpath;

  private final ThreadLocal<ClassLoader> loader = new ThreadLocal<ClassLoader>();

  public TransformingClassLoaderFactory(final Transformation t,
      final IsolationStrategy s) {
    this(new ClassPath(), t, s);
  }

  public TransformingClassLoaderFactory(final ClassPath classPath, // NO_UCD
      final Transformation t, final IsolationStrategy s) {
    this.transformation = t;
    this.s = s;
    this.classpath = classPath;
  }

  public ClassLoader get() {
    if (this.loader.get() == null) {
      this.loader.set(AccessController
          .doPrivileged(new PrivilegedAction<TransformingClassLoader>() {
            public TransformingClassLoader run() {
              return new TransformingClassLoader(
                  TransformingClassLoaderFactory.this.classpath,
                  TransformingClassLoaderFactory.this.transformation,
                  TransformingClassLoaderFactory.this.s, null);
            }
          }));
      IsolationUtils.setContextClassLoader(this.loader.get());
    }
    return this.loader.get();
  }

}
