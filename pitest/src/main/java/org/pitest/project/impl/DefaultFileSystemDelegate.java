package org.pitest.project.impl;

import java.io.File;

/**
 * Default implementation of the {@see FileSystemDelegate} that redirects all calls to the java {@see File} API.
 *
 * @author Aidan Morgan
 */
public class DefaultFileSystemDelegate implements FileSystemDelegate {
  /**
   * @inheritDoc
   */
  public boolean doesFileExist(String file) {
    File f = new File(file);
    return f.exists();
  }
}
