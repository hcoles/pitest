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
package org.pitest.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.pitest.functional.Option;

public class OtherClassLoaderClassPathRoot implements ClassPathRoot {

  private final ClassLoader loader;

  public OtherClassLoaderClassPathRoot(final ClassLoader loader) {
    this.loader = loader;
  }

  @Override
  public Collection<String> classNames() {
    throw new UnsupportedOperationException();
  }

  @Override
  public InputStream getData(final String name) throws IOException {
    // TODO will this work for archives? Need to consider remote hetrogenous os
    return this.loader.getResourceAsStream(name.replace(".", "/") + ".class");
  }

  @Override
  public URL getResource(final String name) throws MalformedURLException {
    return this.loader.getResource(name);
  }

  @Override
  public Option<String> cacheLocation() {
    return Option.none();
  }

}
