package com.example;

import java.io.InputStream;

import org.pitest.util.IsolationUtils;

public class LoadsResourcesFromClassPath {

  public static boolean loadResource() {
    final InputStream stream = IsolationUtils.getContextClassLoader()
        .getResourceAsStream(
            "resource folder with spaces/text in folder with spaces.txt");
    final boolean result = stream != null; // store result to nudge compiler
    // towards single IRETURN
    return result;
  }

}
