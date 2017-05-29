package org.pitest.classpath;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.pitest.functional.FCollection;
import org.pitest.functional.Option;

public class CompoundClassPathRoot implements ClassPathRoot,
    Iterable<ClassPathRoot> {

  private final List<ClassPathRoot> roots = new ArrayList<ClassPathRoot>();

  public CompoundClassPathRoot(final List<ClassPathRoot> roots) {
    this.roots.addAll(wrapToAvoidIOOperations(roots));
  }

  @Override
  public InputStream getData(final String name) throws IOException {
    for (final ClassPathRoot each : this.roots) {
      final InputStream is = each.getData(name);
      if (is != null) {
        return is;
      }
    }
    return null;
  }

  @Override
  public Collection<String> classNames() {
    final List<String> arrayList = new ArrayList<String>();
    for (final ClassPathRoot root : this.roots) {
      arrayList.addAll(root.classNames());
    }
    return arrayList;
  }

  @Override
  public URL getResource(String name) throws MalformedURLException {
    try {
      return findRootForResource(name);
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

  @Override
  public Option<String> cacheLocation() {
    StringBuilder classpath = new StringBuilder();
    for (final ClassPathRoot each : this.roots) {
      final Option<String> additional = each.cacheLocation();
      for (final String path : additional) {
        classpath = classpath.append(File.pathSeparator + path);
      }
    }

    return Option.some(classpath.toString());
  }

  @Override
  public Iterator<ClassPathRoot> iterator() {
    return this.roots.iterator();
  }

  private  static List<ClassPathRoot> wrapToAvoidIOOperations(
      List<ClassPathRoot> roots) {
    return FCollection.map(roots, NameCachingRoot.toCachingRoot());
  }

  
}