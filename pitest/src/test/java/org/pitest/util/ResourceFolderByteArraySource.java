package org.pitest.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.ClassPathRoot;
import java.util.Optional;

public class ResourceFolderByteArraySource implements ClassByteArraySource {


  @Override
  public Optional<byte[]> getBytes(final String classname) {
    final ClassPath cp = new ClassPath(new ResourceFolderClassPathroot());
    try {
      return Optional.ofNullable(cp.getClassData(classname));
    } catch (final IOException ex) {
      throw Unchecked.translateCheckedException(ex);
    }
  }

}

class ResourceFolderClassPathroot implements ClassPathRoot {

  @Override
  public URL getResource(final String name) throws MalformedURLException {
    return null;
  }

  @Override
  public InputStream getData(String name) throws IOException {
    final String path = "sampleClasses/" + name.replace(".", "/")
        + ".class.bin";
    return IsolationUtils.getContextClassLoader().getResourceAsStream(path);
  }

  @Override
  public Collection<String> classNames() {
    return null;
  }

  @Override
  public Optional<String> cacheLocation() {
    return null;
  }

}