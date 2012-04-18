package org.pitest.util;

import static org.junit.Assert.assertArrayEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;


public class StreamUtilTest {

  @Test
  public void shouldCopyStreamsToByteArrays() throws IOException {
    byte[] expected = createByteArray();
    ByteArrayInputStream bis = new ByteArrayInputStream(expected);
    byte[] actual = StreamUtil.streamToByteArray(bis);
    assertArrayEquals(expected,actual);
  }

  private byte[] createByteArray() {
    byte[] expected = {1,2,3,4,5,6,7,8,9,10,0xA};
    return expected;
  }
 
  @Test
  public void shouldCopyContentsOfOneInputStreamToAnother() throws IOException {
    byte[] expected = createByteArray();
    InputStream actualStream = StreamUtil.copyStream(new ByteArrayInputStream(createByteArray()));
    byte[] actualContents = StreamUtil.streamToByteArray(actualStream);
    assertArrayEquals(expected, actualContents);
  }
  
}
