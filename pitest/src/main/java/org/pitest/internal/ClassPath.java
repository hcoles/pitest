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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import org.pitest.PitError;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.internal.classloader.ArchiveClassPathRoot;
import org.pitest.internal.classloader.ClassPathRoot;
import org.pitest.internal.classloader.DirectoryClassPathRoot;
import org.pitest.internal.classloader.PITClassLoader;

public class ClassPath implements Iterable<ClassPathRoot> {

  private static final Logger       LOGGER = Logger.getLogger(ClassPath.class
                                               .getName());

  private final List<ClassPathRoot> roots  = new ArrayList<ClassPathRoot>();

  public ClassPath() {
    this(ClassPath.getClassPathElementsAsFiles());
  }

  public ClassPath(final ClassPathRoot... roots) {
    this.roots.addAll(Arrays.asList(roots));
  }

  public ClassPath(final Collection<File> files) {
    this(files, false);
  }

  public ClassPath(final Collection<File> files, final boolean declareCaches) {
    final F<File, Boolean> exists = new F<File, Boolean>() {
      public Boolean apply(final File a) {
        return a.exists() && a.canRead();
      }
    };
    this.roots.addAll(createRoots(FCollection.filter(files, exists),
        declareCaches));
  }

  public static ClassPath createFrom(final ClassLoader loader) {
    // hmm
    if (loader instanceof PITClassLoader) {
      return ((PITClassLoader) loader).getClassPath();
    } else {
      return new ClassPath();
    }
  }

  public Collection<String> classNames() {
    final List<String> arrayList = new ArrayList<String>();
    for (final ClassPathRoot root : this.roots) {
      arrayList.addAll(root.classNames());
    }
    return arrayList;
  }

  // fixme should not be determining type here
  private Collection<ClassPathRoot> createRoots(final Collection<File> files,
      final boolean declareCaches) {
    File lastFile = null;
    try {
      final List<ClassPathRoot> rs = new ArrayList<ClassPathRoot>();

      for (final File f : files) {
        lastFile = f;
        if (f.isDirectory()) {
          rs.add(new DirectoryClassPathRoot(f, declareCaches));
        } else {
          try {
            rs.add(new ArchiveClassPathRoot(f, declareCaches));
          } catch (final ZipException ex) {
            LOGGER.warning("Can't open the archive " + f);
          }
        }
      }
      return rs;
    } catch (final IOException ex) {
      throw new PitError("Error handling file " + lastFile, ex);
    }
  }

  public byte[] getClassData(final String classname) throws IOException {
    byte[] b = null;
    for (final ClassPathRoot root : this.roots) {
      final InputStream s = root.getData(classname);
      if (s != null) {
        b = streamToByteArray(s);
        s.close();
        break;
      }
    }
    return b;
  }

  public static byte[] streamToByteArray(final InputStream in)
      throws IOException {
    final byte[] array = new byte[in.available()];
    final ByteArrayOutputStream out = new ByteArrayOutputStream(array.length);
    int length = in.read(array);
    while (length > 0) {
      out.write(array, 0, length);
      length = in.read(array);
    }
    return out.toByteArray();
  }

  public URL findResource(final String name) {
    try {
      final URL url = findRootForResource(name);
      return url;
    } catch (final IOException exception) {
      return null;
    }
  }

  private URL findRootForResource(final String name) throws IOException {
    for (final ClassPathRoot root : this.roots) {
      final URL u = root.getResource(name);
      if (u != null) {
        return u;
      }
    }
    return null;
  }

  public static Collection<File> getClassPathElementsAsFiles() {

    final String[] elements = getClassPathElements();

    final Set<File> us = new LinkedHashSet<File>();
    for (final String each : elements) {
      us.add(new File(each));
    }
    return us;

  }

  public void addRoot(final ClassPathRoot root) {
    this.roots.add(root);
  }

  /** FIXME move somewhere common */
  public static String[] getClassPathElements() {
    final String classPath = System.getProperty("java.class.path");
    final String separator = System.getProperty("path.separator");
    if (classPath != null) {
      return classPath.split(separator);
    } else {
      return new String[0];
    }

  }

  public Iterator<ClassPathRoot> iterator() {
    return this.roots.iterator();
  }

  public Collection<String> findClasses(final Predicate<String> nameFilter) {
    return FCollection.filter(classNames(), nameFilter);
  }

}
