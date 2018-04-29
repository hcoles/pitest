package org.pitest.util;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class StreamUtilTest {

  @Test
  public void shouldCopyStreamsToByteArrays() throws IOException {
    final byte[] expected = createByteArray();
    final ByteArrayInputStream bis = new ByteArrayInputStream(expected);
    final byte[] actual = StreamUtil.streamToByteArray(bis);
    assertArrayEquals(expected, actual);
  }

  private byte[] createByteArray() {
    final byte[] expected = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 0xA };
    return expected;
  }

  @Test
  public void shouldCopyContentsOfOneInputStreamToAnother() throws IOException {
    final byte[] expected = createByteArray();
    final InputStream actualStream = StreamUtil
        .copyStream(new ByteArrayInputStream(createByteArray()));
    final byte[] actualContents = StreamUtil.streamToByteArray(actualStream);
    assertArrayEquals(expected, actualContents);
  }

}
