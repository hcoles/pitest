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
package org.pitest.simpletest;

import org.pitest.classpath.ClassPath;
import org.pitest.mutationtest.execute.DefaultPITClassloader;
import org.pitest.util.IsolationUtils;

public class TransformingClassLoader extends DefaultPITClassloader {

  private final Transformation    transformation;
  private final IsolationStrategy isolationStrategy;

  public TransformingClassLoader(final Transformation transformation,
      final IsolationStrategy isolationStrategy) {
    this(new ClassPath(), transformation, isolationStrategy, IsolationUtils
        .bootClassLoader());
  }

  public TransformingClassLoader(final ClassPath classPath,
      final Transformation transformation,
      final IsolationStrategy isolationStrategy, final ClassLoader parent) {
    super(classPath, parent);
    this.transformation = transformation;
    this.isolationStrategy = isolationStrategy;
  }

  @Override
  protected Class<?> defineClass(final String name, final byte[] bytes) {
    final byte[] b = transform(name, bytes);
    return defineClass(name, b, 0, b.length);
  }

  private byte[] transform(final String name, final byte[] bytes) {
    if (this.isolationStrategy.shouldIsolate(name)) {
      return this.transformation.transform(name, bytes);
    } else {
      return bytes;
    }
  }

}
