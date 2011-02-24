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
package org.pitest.distributed;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.pitest.functional.Option;
import org.pitest.internal.classloader.ClassPathRoot;

public class DistributedCacheRoot implements ClassPathRoot {

  private final Map<String, byte[]> cache;

  public DistributedCacheRoot(final Map<String, byte[]> cache) {
    this.cache = cache;
  }

  public Collection<String> classNames() {
    return Collections.<String> emptyList();
  }

  public InputStream getData(final String name) throws IOException {
    final byte[] bytes = this.cache.get(name);
    if (bytes != null) {
      return new ByteArrayInputStream(bytes);
    } else {
      return null;
    }
  }

  public URL getResource(final String name) throws MalformedURLException {
    // we do not cache resources
    return null;
  }

  public Option<String> cacheLocation() {
    return Option.none();
  }

}
