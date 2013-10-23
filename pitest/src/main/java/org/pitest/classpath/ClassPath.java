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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.zip.ZipException;

import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.predicate.Predicate;
import org.pitest.util.Log;
import org.pitest.util.PitError;
import org.pitest.util.StreamUtil;

public class ClassPath {

  private final static Logger       LOG   = Log.getLogger();

  private final CompoundClassPathRoot root;

  public ClassPath() {
    this(ClassPath.getClassPathElementsAsFiles());
  }

  public ClassPath(final ClassPathRoot... roots) {
    this(Arrays.asList(roots));
  }
  
  public ClassPath(List<ClassPathRoot> roots) {
    this.root = new CompoundClassPathRoot(roots);
  }

  public ClassPath(final Collection<File> files) {
    this(createRoots(FCollection.filter(files, exists())));
  }

  private static F<File, Boolean> exists() {
    return new F<File, Boolean>() {
      public Boolean apply(final File a) {
        return a.exists() && a.canRead();
      }
    };
  }

  public Collection<String> classNames() {
    return root.classNames();
  }

  // fixme should not be determining type here
  private static List<ClassPathRoot> createRoots(final Collection<File> files) {
    File lastFile = null;
    try {
      final List<ClassPathRoot> rs = new ArrayList<ClassPathRoot>();

      for (final File f : files) {
        lastFile = f;
        if (f.isDirectory()) {
          rs.add(new DirectoryClassPathRoot(f));
        } else {
          try {
            if (!f.canRead()) {
              throw new IOException("Can't read the file " + f);
            }
            rs.add(new ArchiveClassPathRoot(f));
          } catch (final ZipException ex) {
            LOG.warning("Can't open the archive " + f);
          }
        }
      }
      return rs;
    } catch (final IOException ex) {
      throw new PitError("Error handling file " + lastFile, ex);
    }
  }

  public byte[] getClassData(final String classname) throws IOException {
    InputStream is = root.getData(classname);
    if ( is != null ) {
      try {
      return StreamUtil.streamToByteArray(is);
      } finally {
        is.close();
      }
    }
    return null;  
  }

  public URL findResource(final String name) {
    try {
      return root.getResource(name);
    } catch (final IOException exception) {
      return null;
    }
  }

  public static Collection<File> getClassPathElementsAsFiles() {

    final String[] elements = getClassPathElements();

    final Set<File> us = new LinkedHashSet<File>();
    for (final String each : elements) {
      us.add(new File(each));
    }
    return us;

  }

  /** FIXME move somewhere common */
  public static String[] getClassPathElements() {
    final String classPath = System.getProperty("java.class.path");
    final String separator = File.pathSeparator;
    if (classPath != null) {
      return classPath.split(separator);
    } else {
      return new String[0];
    }

  }


  public Collection<String> findClasses(final Predicate<String> nameFilter) {
    return FCollection.filter(classNames(), nameFilter);
  }

  public String getLocalClassPath() {
    return root.cacheLocation().value();
  }
  
  public ClassPath getComponent(final Predicate<ClassPathRoot> predicate) {
    return new ClassPath(FCollection.filter(this.root, predicate).toArray(
        new ClassPathRoot[0]));
  }
  
  public CompoundClassPathRoot asRoot() {
    return this.root;
  }

}
