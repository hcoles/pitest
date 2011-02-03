package org.pitest.util;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.pitest.internal.IsolationUtils;

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

  public static String readToString(final InputStream is)
      throws java.io.IOException {
    final StringBuffer fileData = new StringBuffer(1000);
    final BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    char[] buf = new char[1024];
    int numRead = 0;

    while ((numRead = reader.read(buf)) != -1) {
      final String readData = String.valueOf(buf, 0, numRead);
      fileData.append(readData);
      buf = new char[1024];
    }

    reader.close();
    return fileData.toString();
  }

  public static void writeBytesFromClassPathToFile(final File dest,
      final String resource) {

    BufferedOutputStream bos = null;
    try {
      final InputStream is = IsolationUtils.getContextClassLoader()
          .getResourceAsStream(resource);
      bos = new BufferedOutputStream(new FileOutputStream(dest));

      int i;
      final byte[] buf = new byte[256];
      while ((i = is.read(buf, 0, buf.length)) >= 0) {
        bos.write(buf, 0, i);
      }

    } catch (final IOException e) {

      System.out.println("No longer able to read stream");

    } finally {
      if (bos != null) {
        try {
          bos.close();
        } catch (final IOException e) {
          throw Unchecked.translateCheckedException(e);
        }
      }
    }
  }

}
