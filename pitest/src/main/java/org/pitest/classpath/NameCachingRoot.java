package org.pitest.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.function.Function;

import org.pitest.classinfo.ClassName;
import java.util.Optional;

/**
 * Caches the classes provided by the decorated
 * root to avoid expensive IO operations at the
 * cost of higher memory consumption
 */
public class NameCachingRoot implements ClassPathRoot {

  private final ClassPathRoot child;

  private SoftReference<Collection<String>> cache;

  public NameCachingRoot(ClassPathRoot child) {
    this.child = child;
  }

  @Override
  public URL getResource(String name) throws MalformedURLException {
    return this.child.getResource(name);
  }

  @Override
  public InputStream getData(String name) throws IOException {
    final Collection<String> names = classNames();
    if (!names.contains(ClassName.fromString(name).asJavaName())) {
      return null;
    }
    return this.child.getData(name);
  }

  @Override
  public Collection<String> classNames() {
    if (this.cache != null) {
      final Collection<String> cachedNames = this.cache.get();
      if (cachedNames != null) {
        return cachedNames;
      }
    }
    final Collection<String> names = this.child.classNames();
    this.cache = new SoftReference<>(names);
    return  names;
  }

  @Override
  public Optional<String> cacheLocation() {
    return this.child.cacheLocation();
  }

  public static Function<ClassPathRoot, ClassPathRoot> toCachingRoot() {
     return a -> {
      // ugly hack to determine where caching will be useful
      if (a instanceof IOHeavyRoot ) {
        return new NameCachingRoot(a);
      }
      return a;
    };
  }

}
