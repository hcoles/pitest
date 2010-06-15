package org.pitest.util;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Random;

import org.junit.Test;

public class Base64Test {
  private static final long SEED     = 12345678;
  private static Random     s_random = new Random(SEED);

  @Test
  public void testStreams() throws Exception {
    for (int i = 0; i < 100; ++i) {
      runStreamTest(i);
    }
    for (int i = 100; i < 2000; i += 250) {
      runStreamTest(i);
    }
    for (int i = 2000; i < 80000; i += 1000) {
      runStreamTest(i);
    }
  }

  private byte[] createData(final int length) throws Exception {
    final byte[] bytes = new byte[length];
    s_random.nextBytes(bytes);
    return bytes;
  }

  private void runStreamTest(final int length) throws Exception {
    final byte[] data = createData(length);
    ByteArrayOutputStream out_bytes = new ByteArrayOutputStream();
    final OutputStream out = new Base64.OutputStream(out_bytes);
    out.write(data);
    out.close();
    final byte[] encoded = out_bytes.toByteArray();
    byte[] decoded = Base64.decode(encoded, 0, encoded.length, 0);
    assertTrue(Arrays.equals(data, decoded));

    final Base64.InputStream in = new Base64.InputStream(
        new ByteArrayInputStream(encoded));
    out_bytes = new ByteArrayOutputStream();
    final byte[] buffer = new byte[3];
    for (int n = in.read(buffer); n > 0; n = in.read(buffer)) {
      out_bytes.write(buffer, 0, n);
    }
    out_bytes.close();
    in.close();
    decoded = out_bytes.toByteArray();
    assertTrue(Arrays.equals(data, decoded));
  }

}
