package org.pitest.classpath;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import java.util.Optional;

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
public class DirectoryClassPathRoot implements ClassPathRoot, IOHeavyRoot {

  private final File root;

  public DirectoryClassPathRoot(final File root) {
    this.root = root;
  }

  @Override
  public InputStream getData(final String classname) throws IOException {
    final String filename = classname.replace('.', File.separatorChar).concat(
        ".class");
    final File file = new File(this.root, filename);
    if (file.canRead()) {
      return new FileInputStream(file);
    } else {
      return null;
    }
  }

  @Override
  public URL getResource(final String name) throws MalformedURLException {
    final File f = new File(this.root, name);
    if (f.canRead()) {
      // magically work around encoding issues
      return f.toURI().toURL();
    } else {
      return null;
    }
  }

  @Override
  public Collection<String> classNames() {
    return classNames(this.root);
  }

  private Collection<String> classNames(final File file) {
    final List<String> classNames = new LinkedList<>();
    for (final File f : file.listFiles()) {
      if (f.isDirectory()) {
        classNames.addAll(classNames(f));
      } else if (f.getName().endsWith(".class")) {
        classNames.add(fileToClassName(f));
      }
    }
    return classNames;
  }

  private String fileToClassName(final File f) {
    return f
        .getAbsolutePath()
        .substring(this.root.getAbsolutePath().length() + 1,
            (f.getAbsolutePath().length() - ".class".length()))
            .replace(File.separatorChar, '.');
  }

  @Override
  public Optional<String> cacheLocation() {
    return Optional.ofNullable(this.root.getAbsolutePath());
  }

}
