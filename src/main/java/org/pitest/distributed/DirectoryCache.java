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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.pitest.distributed.message.RunDetails;
import org.pitest.functional.Option;
import org.pitest.util.FileUtil;

public class DirectoryCache implements ResourceCache {

  private final File cacheDirectory;

  public DirectoryCache(final RunDetails run) {
    this.cacheDirectory = new File("classpathCache" + File.separator
        + run.getIdentifier());
    if (!this.cacheDirectory.exists() && !this.cacheDirectory.mkdirs()) {
      throw new RuntimeException("Could not create cache dir "
          + this.cacheDirectory.getAbsolutePath());
    }

  }

  public Option<URL> getResource(final String name) throws IOException {
    final File resource = new File(this.cacheDirectory.getAbsolutePath()
        + File.separator + name);
    if (resource.exists()) {
      return Option.someOrNone(resource.toURI().toURL());
    } else {
      return Option.none();
    }
  }

  public URL cacheResource(final String name, final byte[] data)
      throws IOException {

    if (getResource(name).hasNone()) {
      final File resource = new File(this.cacheDirectory.getAbsolutePath()
          + File.separator + name);

      createParentFolderIfDoesNotExist(resource);

      FileOutputStream fos = null;
      try {
        fos = new FileOutputStream(resource);
        fos.write(data, 0, data.length);
      } finally {
        if (fos != null) {
          fos.close();
        }
      }

    }
    return getResource(name).value();

  }

  private void createParentFolderIfDoesNotExist(final File resource) {
    if (!resource.getParentFile().exists()) {
      if (!resource.getParentFile().mkdirs()) {
        throw new RuntimeException("Could not create folder structure for "
            + resource);
      }
    }
  }

  public void destroy() {
    try {
      FileUtil.deleteDirectory(this.cacheDirectory);
    } catch (final Throwable t) {
      t.printStackTrace();
    }

  }

  public Option<String> cacheLocation() {
    return Option.someOrNone(this.cacheDirectory.getAbsolutePath());
  }

}
