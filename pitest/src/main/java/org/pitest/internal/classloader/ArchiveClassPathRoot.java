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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.pitest.functional.Option;
import org.pitest.util.Unchecked;

public class ArchiveClassPathRoot implements ClassPathRoot {

  private final boolean   declareCaches;
  private final File      file;
  private Option<ZipFile> root;

  public ArchiveClassPathRoot(final File file) throws IOException {
    this(file, false);
  }

  public ArchiveClassPathRoot(final File file, final boolean declareCaches)
      throws IOException {
    this.file = file;
    this.root = Option.none();
    this.declareCaches = declareCaches;
    if (!file.canRead()) {
      throw new IOException("Can't read the file " + file);
    }
  }

  private ZipFile getRoot() {
    try {
      synchronized (this.file) {

        if (this.root.hasNone()) {
          this.root = Option.some(new ZipFile(this.file));
        }
      }
      return this.root.value();
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

  public InputStream getData(final String name) throws IOException {
    final ZipEntry entry = this.getRoot().getEntry(
        name.replace('.', '/') + ".class");
    if (entry == null) {
      return null;
    }
    return this.getRoot().getInputStream(entry);
  }

  public URL getResource(final String name) throws MalformedURLException {
    final ZipEntry entry = this.getRoot().getEntry(name);
    if (entry != null) {
      return new URL("jar:file:" + this.getRoot().getName() + "!/"
          + entry.getName());
    } else {
      return null;
    }

  }

  @Override
  public String toString() {
    return "ArchiveClassPathRoot [file=" + this.file.getName() + "]";
  }

  public Collection<String> classNames() {
    final List<String> names = new ArrayList<String>();
    final Enumeration<? extends ZipEntry> entries = this.getRoot().entries();
    while (entries.hasMoreElements()) {
      final ZipEntry entry = entries.nextElement();
      if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
        names.add(stringToClassName(entry.getName()));
      }
    }
    return names;

  }

  private String stringToClassName(final String name) {
    return name.substring(0, (name.length() - ".class".length())).replace('/',
        '.');
  }

  public Option<String> cacheLocation() {
    if (this.declareCaches) {
      return Option.some(this.file.getAbsolutePath());
    }
    return Option.none();
  }

}
