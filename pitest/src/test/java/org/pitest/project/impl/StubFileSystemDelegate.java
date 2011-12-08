package org.pitest.project.impl;

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
   * The {@see Map} that holds the canned responses.
   */
  private Map<String, Boolean> resultMap;

  /**
   * Constructor
   */
  public StubFileSystemDelegate() {
    resultMap = new HashMap<String, Boolean>();
  }

  /**
   * Adds the provided canned response for the provided file string.
   * @param s the file string to record a response for
   * @param b the response to give for the provided file string.
   */
  public void addResult(String s, boolean b) {
    this.resultMap.put(s, b);
  }

  /**
   * @inheritDoc
   */
  public boolean doesFileExist(String file) {
    if(resultMap.containsKey(file)) {
      return resultMap.get(file);
    }

    return false;
  }
}
