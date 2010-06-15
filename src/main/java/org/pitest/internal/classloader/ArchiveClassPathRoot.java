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
package org.pitest.internal.classloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ArchiveClassPathRoot implements ClassPathRoot {

  private final ZipFile root;

  public ArchiveClassPathRoot(final File file) throws IOException {
    this.root = new ZipFile(file);
  }

  public InputStream getData(final String name) throws IOException {
    final ZipEntry entry = this.root
        .getEntry(name.replace('.', '/') + ".class");
    if (entry == null) {
      return null;
    }
    return this.root.getInputStream(entry);
  }

  public URL getResource(final String name) throws MalformedURLException {
    final ZipEntry entry = this.root.getEntry(name);
    if (entry != null) {
      return new URL("jar:file:" + this.root.getName() + "!/" + entry.getName());
    } else {
      return null;
    }

  }

  public void release() throws IOException {
    this.root.close();
  }

  @Override
  public String toString() {
    return "ArchiveClassPathRoot [root=" + this.root + "]";
  }

  public Collection<String> classNames() {
    // TODO Auto-generated method stub
    return null;
  }

}
