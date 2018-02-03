package org.pitest.bytecode;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;

public class FrameOptionsTest {

  @Test
  public void shouldComputeFramesForJava7() {
    assertEquals(ClassWriter.COMPUTE_FRAMES, bytesToTestee("CAFEBABE00000033"));
  }

  @Test
  public void shouldComputeFramesForJava8() {
    assertEquals(ClassWriter.COMPUTE_FRAMES, bytesToTestee("CAFEBABE00000034"));
  }

  @Test
  public void shouldNotComputeFramesForJava6() {
    assertEquals(ClassWriter.COMPUTE_MAXS, bytesToTestee("CAFEBABE00000032"));
  }

  @Test
  public void shouldNotComputeFramesForJava5() {
    assertEquals(ClassWriter.COMPUTE_MAXS, bytesToTestee("CAFEBABE00000031"));
  }

  @Test
  public void shouldNotComputeFramesForJava4() {
    assertEquals(ClassWriter.COMPUTE_MAXS, bytesToTestee("CAFEBABE00000030"));
  }

  @Test
  public void shouldNotComputeFramesForJava3() {
    assertEquals(ClassWriter.COMPUTE_MAXS, bytesToTestee("CAFEBABE0000002F"));
  }

  @Test
  public void shouldNotComputeFramesForJava2() {
    assertEquals(ClassWriter.COMPUTE_MAXS, bytesToTestee("CAFEBABE0000002E"));
  }

  @Test
  public void shouldNotComputeFramesForJava1() {
    assertEquals(ClassWriter.COMPUTE_MAXS, bytesToTestee("CAFEBABE0000002D"));
  }

  private int bytesToTestee(String hex) {
    return FrameOptions.pickFlags(toByteArray(hex));
  }

  private static byte[] toByteArray(String s) {
    final int len = s.length();
    final byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
          .digit(s.charAt(i + 1), 16));
    }
    return data;
  }
}
