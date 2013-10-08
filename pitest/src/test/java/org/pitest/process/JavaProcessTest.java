package org.pitest.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import org.junit.Test;
import org.pitest.classpath.ClassPath;
import org.pitest.functional.SideEffect1;
import org.pitest.util.NullJavaAgent;

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
    DefaultJavaExecutableLocator je = new DefaultJavaExecutableLocator();
    final JavaProcess jp = JavaProcess.launch(
        new File(System.getProperty("user.dir")), je.javaExecutable(), nullHandler(), nullHandler(),
        Collections.<String> emptyList(), JavaProcessTest.class,
        Collections.<String> emptyList(), NullJavaAgent.instance(),
        new ClassPath().getLocalClassPath());
    assertTrue(jp.isAlive());
    assertEquals(EXIT_CODE, jp.waitToDie());
  }

  private SideEffect1<String> nullHandler() {
    return new SideEffect1<String>() {
      public void apply(final String a) {

      }

    };
  }

}
