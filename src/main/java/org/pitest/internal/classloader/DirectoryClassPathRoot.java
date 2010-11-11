package org.pitest.internal.classloader;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.pitest.functional.Option;

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
public class DirectoryClassPathRoot implements ClassPathRoot {

  private final File root;

  public DirectoryClassPathRoot(final File root) {
    this.root = root;
  }

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

  public URL getResource(final String name) throws MalformedURLException {
    final File f = new File(this.root, name);
    if (f.canRead()) {
      // magically work around encoding issues
      return f.toURI().toURL();
    } else {
      return null;
    }
  }

  public void release() throws IOException {
    // nothing to release
  }

  @Override
  public String toString() {
    return "DirectoryClassPathRoot [root=" + this.root + "]";
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((this.root == null) ? 0 : this.root.hashCode());
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final DirectoryClassPathRoot other = (DirectoryClassPathRoot) obj;
    if (this.root == null) {
      if (other.root != null) {
        return false;
      }
    } else if (!this.root.equals(other.root)) {
      return false;
    }
    return true;
  }

  public Collection<String> classNames() {
    return classNames(this.root);
  }

  private Collection<String> classNames(final File file) {
    final List<String> classNames = new LinkedList<String>();
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
    return f.getAbsolutePath().substring(
        this.root.getAbsolutePath().length() + 1,
        (f.getAbsolutePath().length() - ".class".length())).replace(
        File.separatorChar, '.');
  }

  public Option<String> cacheLocation() {
    return Option.none();
  }

}
