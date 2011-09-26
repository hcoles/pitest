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
package org.pitest.containers;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.concurrent.Executors;

import org.pitest.extension.ClassLoaderFactory;
import org.pitest.extension.IsolationStrategy;
import org.pitest.extension.Transformation;
import org.pitest.extension.common.AllwaysIsolateStrategy;
import org.pitest.internal.ClassPath;
import org.pitest.internal.TransformingClassLoaderFactory;
import org.pitest.internal.transformation.IdentityTransformation;
import org.pitest.util.Unchecked;

public class IsolatedThreadPoolContainer extends BaseThreadPoolContainer {

  private static class URLClassLoaderFatory implements ClassLoaderFactory {

    public ClassLoader get() {
      try {
        final Collection<File> files = ClassPath.getClassPathElementsAsFiles();
        final URL[] urls = new URL[files.size()];
        int i = 0;
        for (final File each : files) {
          urls[i] = new URL("file:" + each.getAbsolutePath());
          i++;
        }

        return new URLClassLoader(urls);
      } catch (final Exception ex) {
        throw Unchecked.translateCheckedException(ex);
      }
    }

  }

  public IsolatedThreadPoolContainer(final Integer threads) {
    this(threads, false);
  }

  public IsolatedThreadPoolContainer(final Integer threads,
      final boolean usePitClassloader) {
    super(threads, pickLoaderFactory(usePitClassloader), Executors
        .defaultThreadFactory());

  }

  private static ClassLoaderFactory pickLoaderFactory(
      final boolean usePitClassloader) {
    if (usePitClassloader) {
      final IsolationStrategy i = new AllwaysIsolateStrategy();
      final Transformation t = new IdentityTransformation();
      return new TransformingClassLoaderFactory(t, i);
    } else {
      return new URLClassLoaderFatory();
    }
  }

}
