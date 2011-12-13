package org.pitest.project.impl;

import java.io.IOException;
import java.io.InputStream;

/**
 * A simple interface that wraps any access to the file system from the {@see
 * DefaultProjectFileParser} implementation, allowing any access to the file
 * system to be intercepted.
 * 
 * @author Aidan Morgan
 */
public interface FileSystemDelegate {
  public boolean exists(String f);

  public boolean isFile(String projectFile);

  public boolean canRead(String projectFile);

  public InputStream openStream(String projectFile) throws IOException;

}
