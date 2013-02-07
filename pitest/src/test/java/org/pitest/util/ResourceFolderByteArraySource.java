package org.pitest.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.pitest.functional.Option;
import org.pitest.internal.ClassByteArraySource;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.ClassPathRoot;

public class ResourceFolderByteArraySource implements ClassByteArraySource {

  public Option<byte[]> apply(final String classname) {
    final ClassPath cp = new ClassPath(new ResourceFolderClassPathroot());
    try {
      return Option.some(cp.getClassData(classname));
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }

  }

}

class ResourceFolderClassPathroot implements ClassPathRoot {

  public URL getResource(final String name) throws MalformedURLException {
    return null;
  }

  public InputStream getData(final String name) throws IOException {
    final String path = "sampleClasses/" + name.replace(".", "/")
        + ".class.bin";
    return IsolationUtils.getContextClassLoader().getResourceAsStream(path);
  }

  public Collection<String> classNames() {
    return null;
  }

  public Option<String> cacheLocation() {
    return null;
  }

}