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

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.pitest.distributed.master.MasterService;
import org.pitest.functional.Option;
import org.pitest.internal.classloader.ClassPathRoot;

public class RemoteRoot implements ClassPathRoot {

  private final MasterService service;
  private final ResourceCache cache;

  public RemoteRoot(final MasterService service,
      final ResourceCache resourceCache) {
    this.service = service;
    this.cache = resourceCache;
  }

  public Collection<String> classNames() {
    throw new UnsupportedOperationException();
  }

  public InputStream getData(final String name) throws IOException {
    final byte[] data = this.service.getClasspathData(name);
    if (data != null) {
      return new ByteArrayInputStream(data);
    } else {
      return null;
    }

  }

  public URL getResource(final String name) throws MalformedURLException {

    try {

      final Option<URL> maybeUrl = this.cache.getResource(name);
      if (maybeUrl.hasSome()) {
        return maybeUrl.value();
      } else {
        final Option<byte[]> maybeBytes = this.service.getResourceData(name);
        if (maybeBytes.hasSome()) {
          return this.cache.cacheResource(name, maybeBytes.value());
        } else {
          return null;
        }
      }

    } catch (final IOException ex) {
      throw translateCheckedException(ex);
    }

  }

  public void release() throws IOException {
    // TODO Auto-generated method stub

  }

}
