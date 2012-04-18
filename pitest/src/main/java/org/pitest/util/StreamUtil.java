package org.pitest.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public abstract class StreamUtil {

  public static byte[] streamToByteArray(final InputStream in)
      throws IOException {
    final ByteArrayOutputStream result = new ByteArrayOutputStream();
    copy(in, result);
    result.close();
    return result.toByteArray();
  }

  public static InputStream copyStream(final InputStream in) throws IOException {
    final byte[] bs = streamToByteArray(in);
    return new ByteArrayInputStream(bs);
  }

  private static void copy(final InputStream input, final OutputStream output)
      throws IOException {
    final byte[] buffer = new byte[1024];
    int read = input.read(buffer);
    while (read != -1) {
      output.write(buffer, 0, read);
      read = input.read(buffer);
    }
    output.flush();
  }
}
