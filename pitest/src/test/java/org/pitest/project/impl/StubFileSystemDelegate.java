package org.pitest.project.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A stub implementation of the {@see FileSystemDelegate} that allows canned responses for
 * specific file strings to be set up and returned.
 *
 * @author Aidan Morgan
 */
public class StubFileSystemDelegate implements FileSystemDelegate {
  /**
   * {@code true} if the provided file string exists, {@code false} otherwise.
   */
  private boolean fileExists = true;

  /**
   * {@code true} if the provided file is a file, {@code false} otherwise.
   */
  private boolean isFile = true;

  /**
   * {@code true} if the provided file can be read, {@code false} otherwise.
   */
  private boolean canRead = true;

  /**
   * The {@see InputStream} to return for a provided file string.
   */
  private InputStream inputStream;
  private Map<String, Boolean> exists;

  /**
   * Constructor
   */
  public StubFileSystemDelegate() {
    this((InputStream) null);
  }

  /**
   * Constructor.
   * <p/>
   * Initialises the {@see StubFileSystemDelegate} as if the file exists, is a file and can be read.
   *
   * @param inputStream the {@see InputStream} to return when the {@see FileSystemDelegate.openStream} method is called.
   */
  public StubFileSystemDelegate(InputStream inputStream) {
    this.inputStream = inputStream;
    this.exists = new HashMap<String, Boolean>();
  }

  /**
   * Constructor.
   * <p/>
   * Initialises the {@see StubFileSystemDelegate} as if the file exists, is a file, and can be read. The
   * {@see FileSystemDelegate.openStream} method will return an {@see InputStream} which wraps the provided
   * {@see fileContents}.
   *
   * @param fileContents the expected contents of the file.
   */
  public StubFileSystemDelegate(String fileContents) {
    this(new ByteArrayInputStream(fileContents.getBytes()));
  }

  /**
   * @inheritDoc
   */
  public boolean exists(String f) {
    if (exists.containsKey(f)) {
      return exists.get(f);
    }

    return fileExists;
  }

  /**
   * @inheritDoc
   */
  public boolean isFile(String projectFile) {
    return isFile;
  }

  /**
   * @inheritDoc
   */
  public boolean canRead(String projectFile) {
    return canRead;
  }

  /**
   * @inheritDoc
   */
  public InputStream openStream(String projectFile) throws IOException {
    return inputStream;
  }

  /**
   * Sets the canned result for the {@see FileSystemDelegate.exists()} method call.
   *
   * @param fileExists the result to return for a {@see FileSystemDelegate.exists()} call.
   */
  public void setFileExists(boolean fileExists) {
    this.fileExists = fileExists;
  }

  /**
   * Sets the canned result for the {@see FileSystemDelegate.isFile()} method call.
   *
   * @param file the result to return for a {@see FileSystemDelegate.isFile()} call.
   */
  public void setFile(boolean file) {
    isFile = file;
  }

  /**
   * Sets the canned result for the {@see FileSystemDelegate.canRead()} method call.
   *
   * @param canRead the result to return for a {@see FileSystemDelegate.canRead()} call.
   */
  public void setCanRead(boolean canRead) {
    this.canRead = canRead;
  }

  /**
   * Sets the canned result for the {@see FileSystemDelegate.openStream()} method call.
   *
   * @param inputStream the result to return for a {@see FileSystemDelegate.openStream()} call.
   */
  public void setInputStream(InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void setFileExists(String s, boolean b) {
    exists.put(s, b);
  }
}
