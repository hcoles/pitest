package org.pitest.classpath;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.Option;

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
    return child.getResource(name);
  }

  @Override
  public InputStream getData(String name) throws IOException {
    Collection<String> names = classNames();
    if (!names.contains(ClassName.fromString(name).asJavaName())) {
      return null;
    }
    return child.getData(name);
  }

  @Override
  public Collection<String> classNames() {
    if (cache != null) {
      Collection<String> cachedNames = cache.get();
      if (cachedNames != null) {
        return cachedNames;
      }
    }
    Collection<String> names = child.classNames();
    cache = new SoftReference<Collection<String>>(names);
    return  names;
  }

  @Override
  public Option<String> cacheLocation() {
    return child.cacheLocation();
  }

  public static F<ClassPathRoot, ClassPathRoot> toCachingRoot() {
     return new F<ClassPathRoot, ClassPathRoot>() {
      @Override
      public ClassPathRoot apply(ClassPathRoot a) {
        // ugly hack to determine where caching will be useful
        if (a instanceof IOHeavyRoot ) {
          return new NameCachingRoot(a);
        }
        return a;
      }
     };
  }

}
