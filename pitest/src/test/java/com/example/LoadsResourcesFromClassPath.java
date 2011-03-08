package com.example;

import java.io.InputStream;

import org.pitest.internal.IsolationUtils;

public class LoadsResourcesFromClassPath {

  public static boolean loadResource() {
    final InputStream stream = IsolationUtils.getContextClassLoader()
        .getResourceAsStream(
            "resource folder with spaces/text in folder with spaces.txt");
    return stream != null;
  }

}
