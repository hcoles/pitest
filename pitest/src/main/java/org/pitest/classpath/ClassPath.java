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

  private static final Logger         LOG = Log.getLogger();

  private final CompoundClassPathRoot root;

  public ClassPath() {
    this(ClassPath.getClassPathElementsAsFiles());
  }

  public ClassPath(final ClassPathRoot... roots) {
    this(Arrays.asList(roots));
  }

  public ClassPath(final Collection<File> files) {
    this(createRoots(FCollection.filter(files, exists())));
  }
  
  public ClassPath(List<ClassPathRoot> roots) {
    this.root = new CompoundClassPathRoot(roots);
  }


  public Collection<String> classNames() {
    return this.root.classNames();
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
          handleArchive(rs, f);
        }
      }
      return rs;
    } catch (final IOException ex) {
      throw new PitError("Error handling file " + lastFile, ex);
    }
  }

  private static void handleArchive(final List<ClassPathRoot> rs, final File f)
      throws IOException {
    try {
      if (!f.canRead()) {
        throw new IOException("Can't read the file " + f);
      }
      rs.add(new ArchiveClassPathRoot(f));
    } catch (final ZipException ex) {
      LOG.warning("Can't open the archive " + f);
    }
  }

  public byte[] getClassData(final String classname) throws IOException {
    InputStream is = this.root.getData(classname);
    if (is != null) {
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
      return this.root.getResource(name);
    } catch (final IOException exception) {
      return null;
    }
  }

  public static Collection<String> getClassPathElementsAsPaths() {
    final Set<String> filesAsString = new LinkedHashSet<String>();
    FCollection.mapTo(getClassPathElementsAsFiles(), fileToString(),
        filesAsString);
    return filesAsString;
  }


  public static Collection<File> getClassPathElementsAsFiles() {
    final Set<File> us = new LinkedHashSet<File>();
    FCollection.mapTo(getClassPathElementsAsAre(), stringToCanonicalFile(), us);
    return us;
  }


  public Collection<String> findClasses(final Predicate<String> nameFilter) {
    return FCollection.filter(classNames(), nameFilter);
  }

  public String getLocalClassPath() {
    return this.root.cacheLocation().value();
  }

  public ClassPath getComponent(final Predicate<ClassPathRoot> predicate) {
    return new ClassPath(FCollection.filter(this.root, predicate).toArray(
        new ClassPathRoot[0]));
  }
  
  private static F<File, Boolean> exists() {
    return new F<File, Boolean>() {
      @Override
      public Boolean apply(final File a) {
        return a.exists() && a.canRead();
      }
    };
  }

  private static F<File, String> fileToString() {
    return new F<File, String>() {
      @Override
      public String apply(File file) {
        return file.getPath();
      }
    };
  }
  
  private static F<String, File> stringToCanonicalFile() {
    return new F<String, File>() {
      @Override
      public File apply(String fileAsString) {
        try {
          return new File(fileAsString).getCanonicalFile();
        } catch (final IOException ex) {
          throw new PitError("Error transforming classpath element "
              + fileAsString, ex);
        }
      }
    };
  }

  /** FIXME move somewhere common */
  private static List<String> getClassPathElementsAsAre() {
    final String classPath = System.getProperty("java.class.path");
    final String separator = File.pathSeparator;
    if (classPath != null) {
      return Arrays.asList(classPath.split(separator));
    } else {
      return new ArrayList<String>();
    }

  }
  
}
