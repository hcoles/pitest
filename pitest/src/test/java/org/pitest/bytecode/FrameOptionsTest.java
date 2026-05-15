package org.pitest.bytecode;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;

public class FrameOptionsTest {

  @Test
  public void shouldComputeFramesForJava7() {
    assertThat(bytesToTestee("CAFEBABE00000033")).isEqualTo(ClassWriter.COMPUTE_FRAMES);
  }

  @Test
  public void shouldComputeFramesForJava8() {
    assertThat(bytesToTestee("CAFEBABE00000034")).isEqualTo(ClassWriter.COMPUTE_FRAMES);
  }

  @Test
  public void shouldNotComputeFramesForJava6() {
    assertThat(bytesToTestee("CAFEBABE00000032")).isEqualTo(ClassWriter.COMPUTE_MAXS);
  }

  @Test
  public void shouldNotComputeFramesForJava5() {
    assertThat(bytesToTestee("CAFEBABE00000031")).isEqualTo(ClassWriter.COMPUTE_MAXS);
  }

  @Test
  public void shouldNotComputeFramesForJava4() {
    assertThat(bytesToTestee("CAFEBABE00000030")).isEqualTo(ClassWriter.COMPUTE_MAXS);
  }

  @Test
  public void shouldNotComputeFramesForJava3() {
    assertThat(bytesToTestee("CAFEBABE0000002F")).isEqualTo(ClassWriter.COMPUTE_MAXS);
  }

  @Test
  public void shouldNotComputeFramesForJava2() {
    assertThat(bytesToTestee("CAFEBABE0000002E")).isEqualTo(ClassWriter.COMPUTE_MAXS);
  }

  @Test
  public void shouldNotComputeFramesForJava1() {
    assertThat(bytesToTestee("CAFEBABE0000002D")).isEqualTo(ClassWriter.COMPUTE_MAXS);
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
