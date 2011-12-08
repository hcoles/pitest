package org.pitest.project.impl;

/**
 * A simple interface that wraps any access to the file system from the {@see DefaultProjectFileParser} implementation,
 * allowing any access to the file system to be intercepted.
 *
 * @author Aidan Morgan
 */
public interface FileSystemDelegate {
  /**
   * Returns {@code true} if the provided file exists on the filesystem, {@code false} otherwise.
   *
   * @param file the file to test if it exists.
   * @return {@code true} if the provided file exists on the filesystem, {@code false} otherwise.
   */
  public boolean doesFileExist(String file);
}
