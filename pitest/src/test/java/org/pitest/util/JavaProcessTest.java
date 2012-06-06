package org.pitest.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.Test;

public class JavaProcessTest {

  private final static int EXIT_CODE = 10;

  public static void main(final String[] args) {
    try {
      System.out.println("Sleeping");
      Thread.sleep(100);
    } catch (final InterruptedException e) {
      e.printStackTrace();
    }
    System.out.println("Exiting");
    System.exit(EXIT_CODE);
  }

  @Test
  public void waitToDieShouldReturnProcessExitCode() throws IOException,
      InterruptedException {
    final JavaProcess jp = JavaProcess.launch(new File(System.getProperty("user.dir")),Collections.<String> emptyList(),
        JavaProcessTest.class, Collections.<String> emptyList(),
        NullJavaAgent.instance());
    assertTrue(jp.isAlive());
    assertEquals(EXIT_CODE, jp.waitToDie());
  }

}
