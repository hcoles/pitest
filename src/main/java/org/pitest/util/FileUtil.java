package org.pitest.util;

import java.io.File;

public class FileUtil {

  public static boolean deleteDirectory(final File path) {
    if (path.exists()) {
      final File[] files = path.listFiles();
      for (final File file : files) {
        if (file.isDirectory()) {
          deleteDirectory(file);
        } else {
          file.delete();
        }
      }
    }
    return (path.delete());
  }

}
