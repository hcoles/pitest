package org.pitest.project.impl;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Default implementation of the {@see FileSystemDelegate} that redirects all calls to the java {@see File} API.
 *
 * @author Aidan Morgan
 */
public class DefaultFileSystemDelegate implements FileSystemDelegate {
  public boolean exists(String f) {
    File file = new File(f);
    return file.exists();
  }

  public boolean isFile(String projectFile) {
    File file = new File(projectFile);
    return file.isFile();
  }

  public boolean canRead(String projectFile) {
    File file = new File(projectFile);
    return file.canRead();
  }

  public InputStream openStream(String projectFile) throws IOException {
    return new FileInputStream(projectFile);
  }
}
