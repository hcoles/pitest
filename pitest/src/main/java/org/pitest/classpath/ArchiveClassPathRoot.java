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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import java.util.Optional;
import org.pitest.util.StreamUtil;
import org.pitest.util.Unchecked;

/**
 * ClassPathRoot wrapping a jar or zip file
 */
public class ArchiveClassPathRoot implements ClassPathRoot, IOHeavyRoot {

  private final File file;

  public ArchiveClassPathRoot(final File file) {
    this.file = file;
  }

  @Override
  public InputStream getData(final String name) throws IOException {
    try (ZipHandle zip = getRoot()) {
      final ZipEntry entry = zip.getEntry(name.replace('.', '/') + ".class");
      if (entry == null) {
        return null;
      }
      return StreamUtil.copyStream(zip.getInputStream(entry));
    } catch (Exception e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  @Override
  public URL getResource(final String name) throws MalformedURLException {
    try (ZipHandle zip = getRoot()) {
      final ZipEntry entry = zip.getEntry(name);
      if (entry != null) {
        return new URL("jar:file:" + zip.getName() + "!/" + entry.getName());
      } else {
        return null;
      }
    } catch (Exception e) {
      throw Unchecked.translateCheckedException(e);
    }

  }

  @Override
  public String toString() {
    return "ArchiveClassPathRoot [file=" + this.file.getName() + "]";
  }

  @Override
  public Collection<String> classNames() {
    final List<String> names = new ArrayList<>();
    try (ZipHandle root = getRoot()) {
      final Enumeration<? extends ZipEntry> entries = root.entries();
      while (entries.hasMoreElements()) {
        final ZipEntry entry = entries.nextElement();
        if (!entry.isDirectory() && entry.getName().endsWith(".class")) {
          names.add(stringToClassName(entry.getName()));
        }
      }
      return names;
    } catch (Exception e) {
      throw Unchecked.translateCheckedException(e);
    }

  }

  private String stringToClassName(final String name) {
    return name.substring(0, (name.length() - ".class".length())).replace('/',
        '.');
  }

  @Override
  public Optional<String> cacheLocation() {
    return Optional.ofNullable(this.file.getAbsolutePath());
  }

  private ZipHandle getRoot() {
    try {
      return new WrappedZip(new ZipFile(this.file));
    } catch (ZipException ex) {
      // We might be passed files that are not archives on the classpath
      // rather than trying to filter these out by naming convention we've opted to
      // handle the error quietly here
      return new NotAZip();
    } catch (IOException ex) {
      throw Unchecked.translateCheckedException(ex.getMessage() + " ("
          + this.file + ")", ex);
    }
  }

}

interface ZipHandle extends AutoCloseable {
  ZipEntry getEntry(String name);

  Enumeration<? extends ZipEntry> entries();

  String getName();

  InputStream getInputStream(ZipEntry entry) throws IOException;
}


class WrappedZip implements ZipHandle {

  private final ZipFile zip;

  WrappedZip(ZipFile zip) {
    this.zip = zip;
  }

  @Override
  public ZipEntry getEntry(String name) {
    return zip.getEntry(name);
  }

  @Override
  public Enumeration<? extends ZipEntry> entries() {
    return zip.entries();
  }

  @Override
  public String getName() {
    return zip.getName();
  }

  @Override
  public InputStream getInputStream(ZipEntry entry) throws IOException {
    return zip.getInputStream(entry);
  }

  @Override
  public void close() throws Exception {
    zip.close();
  }
}

class NotAZip implements ZipHandle {

  @Override
  public ZipEntry getEntry(String name) {
    return null;
  }

  @Override
  public Enumeration<? extends ZipEntry> entries() {
    return Collections.emptyEnumeration();
  }

  @Override
  public String getName() {
    return "";
  }

  @Override
  public InputStream getInputStream(ZipEntry entry) {
    return null;
  }

  @Override
  public void close() throws Exception {
  }
}