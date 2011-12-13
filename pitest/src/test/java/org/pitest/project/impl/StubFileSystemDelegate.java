package org.pitest.project.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A stub implementation of the {@see FileSystemDelegate} that allows canned
 * responses for specific file strings to be set up and returned.
 * 
 * @author Aidan Morgan
 */
public class StubFileSystemDelegate implements FileSystemDelegate {
  /**
   * {@code true} if the provided file string exists, {@code false} otherwise.
   */
  private boolean                    fileExists = true;

  /**
   * {@code true} if the provided file is a file, {@code false} otherwise.
   */
  private boolean                    isFile     = true;

  /**
   * {@code true} if the provided file can be read, {@code false} otherwise.
   */
  private boolean                    canRead    = true;

  /**
   * The {@see InputStream} to return for a provided file string.
   */
  private InputStream                inputStream;
  private final Map<String, Boolean> exists;

  /**
   * Constructor
   */
  public StubFileSystemDelegate() {
    this((InputStream) null);
  }

  /**
   * Constructor.
   * <p/>
   * Initialises the {@see StubFileSystemDelegate} as if the file exists, is a
   * file and can be read.
   * 
   * @param inputStream
   *          the {@see InputStream} to return when the {@see
   *          FileSystemDelegate.openStream} method is called.
   */
  public StubFileSystemDelegate(final InputStream inputStream) {
    this.inputStream = inputStream;
    this.exists = new HashMap<String, Boolean>();
  }

  /**
   * Constructor.
   * <p/>
   * Initialises the {@see StubFileSystemDelegate} as if the file exists, is a
   * file, and can be read. The {@see FileSystemDelegate.openStream} method will
   * return an {@see InputStream} which wraps the provided {@see fileContents}.
   * 
   * @param fileContents
   *          the expected contents of the file.
   */
  public StubFileSystemDelegate(final String fileContents) {
    this(new ByteArrayInputStream(fileContents.getBytes()));
  }

  /**
   * @inheritDoc
   */
  public boolean exists(final String f) {
    if (this.exists.containsKey(f)) {
      return this.exists.get(f);
    }

    return this.fileExists;
  }

  /**
   * @inheritDoc
   */
  public boolean isFile(final String projectFile) {
    return this.isFile;
  }

  /**
   * @inheritDoc
   */
  public boolean canRead(final String projectFile) {
    return this.canRead;
  }

  /**
   * @inheritDoc
   */
  public InputStream openStream(final String projectFile) throws IOException {
    return this.inputStream;
  }

  /**
   * Sets the canned result for the {@see FileSystemDelegate.exists()} method
   * call.
   * 
   * @param fileExists
   *          the result to return for a {@see FileSystemDelegate.exists()}
   *          call.
   */
  public void setFileExists(final boolean fileExists) {
    this.fileExists = fileExists;
  }

  /**
   * Sets the canned result for the {@see FileSystemDelegate.isFile()} method
   * call.
   * 
   * @param file
   *          the result to return for a {@see FileSystemDelegate.isFile()}
   *          call.
   */
  public void setFile(final boolean file) {
    this.isFile = file;
  }

  /**
   * Sets the canned result for the {@see FileSystemDelegate.canRead()} method
   * call.
   * 
   * @param canRead
   *          the result to return for a {@see FileSystemDelegate.canRead()}
   *          call.
   */
  public void setCanRead(final boolean canRead) {
    this.canRead = canRead;
  }

  /**
   * Sets the canned result for the {@see FileSystemDelegate.openStream()}
   * method call.
   * 
   * @param inputStream
   *          the result to return for a {@see FileSystemDelegate.openStream()}
   *          call.
   */
  public void setInputStream(final InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void setFileExists(final String s, final boolean b) {
    this.exists.put(s, b);
  }
}
