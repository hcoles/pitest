package org.pitest.project.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * A stub implementation of the FileSystemDelegate that allows canned responses
 * for specific file strings to be set up and returned.
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
   * The InputStream to return for a provided file string.
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
   * Initialises the StubFileSystemDelegate as if the file exists, is a file and
   * can be read.
   * 
   * @param inputStream
   *          the InputStream to return when the FileSystemDelegate.openStream
   *          method is called.
   */
  public StubFileSystemDelegate(final InputStream inputStream) {
    this.inputStream = inputStream;
    this.exists = new HashMap<String, Boolean>();
  }

  /**
   * Constructor.
   * <p/>
   * Initialises the StubFileSystemDelegate as if the file exists, is a file,
   * and can be read. The FileSystemDelegate.openStream method will return an
   * InputStream which wraps the provided fileContents.
   * 
   * @param fileContents
   *          the expected contents of the file.
   */
  public StubFileSystemDelegate(final String fileContents) {
    this(new ByteArrayInputStream(fileContents.getBytes()));
  }

  public boolean exists(final String f) {
    if (this.exists.containsKey(f)) {
      return this.exists.get(f);
    }

    return this.fileExists;
  }

  public boolean isFile(final String projectFile) {
    return this.isFile;
  }

  public boolean canRead(final String projectFile) {
    return this.canRead;
  }

  public InputStream openStream(final String projectFile) throws IOException {
    return this.inputStream;
  }

  /**
   * Sets the canned result for the FileSystemDelegate.exists() method call.
   * 
   * @param fileExists
   *          the result to return for a FileSystemDelegate.exists() call.
   */
  public void setFileExists(final boolean fileExists) {
    this.fileExists = fileExists;
  }

  /**
   * Sets the canned result for the FileSystemDelegate.isFile() method call.
   * 
   * @param file
   *          the result to return for a FileSystemDelegate.isFile() call.
   */
  public void setFile(final boolean file) {
    this.isFile = file;
  }

  /**
   * Sets the canned result for the FileSystemDelegate.canRead() method call.
   * 
   * @param canRead
   *          the result to return for a FileSystemDelegate.canRead() call.
   */
  public void setCanRead(final boolean canRead) {
    this.canRead = canRead;
  }

  /**
   * Sets the canned result for the FileSystemDelegate.openStream() method call.
   * 
   * @param inputStream
   *          the result to return for a FileSystemDelegate.openStream() call.
   */
  public void setInputStream(final InputStream inputStream) {
    this.inputStream = inputStream;
  }

  public void setFileExists(final String s, final boolean b) {
    this.exists.put(s, b);
  }
}
