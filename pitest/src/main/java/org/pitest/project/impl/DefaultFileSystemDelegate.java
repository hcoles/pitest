package org.pitest.project.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Default implementation of the {@see FileSystemDelegate} that redirects all
 * calls to the java {@see File} API.
 * 
 * @author Aidan Morgan
 */
public class DefaultFileSystemDelegate implements FileSystemDelegate {
  public boolean exists(final String f) {
    final File file = new File(f);
    return file.exists();
  }

  public boolean isFile(final String projectFile) {
    final File file = new File(projectFile);
    return file.isFile();
  }

  public boolean canRead(final String projectFile) {
    final File file = new File(projectFile);
    return file.canRead();
  }

  public InputStream openStream(final String projectFile) throws IOException {
    return new FileInputStream(projectFile);
  }
}
