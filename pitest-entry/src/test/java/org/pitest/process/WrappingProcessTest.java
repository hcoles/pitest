package org.pitest.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;

import org.junit.Test;
import org.pitest.classpath.ClassPath;
import org.pitest.functional.SideEffect1;
import org.pitest.util.NullJavaAgent;

public class WrappingProcessTest {

  private static final int EXIT_CODE = 10;

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

    LaunchOptions launchOptions = new LaunchOptions(NullJavaAgent.instance(),
        new DefaultJavaExecutableLocator(), Collections.<String> emptyList(),
        new HashMap<String, String>());

    ProcessArgs processArgs = ProcessArgs
        .withClassPath(new ClassPath().getLocalClassPath())
        .andBaseDir(new File(System.getProperty("user.dir")))
        .andLaunchOptions(launchOptions).andStdout(nullHandler())
        .andStderr(nullHandler());

    WrappingProcess wrappingProcess = new WrappingProcess(-1, processArgs,
        getClass());
    wrappingProcess.start();
    JavaProcess process = wrappingProcess.getProcess();

    assertTrue(process.isAlive());
    assertEquals(EXIT_CODE, process.waitToDie());
  }

  private SideEffect1<String> nullHandler() {
    return new SideEffect1<String>() {
      @Override
      public void apply(final String a) {

      }

    };
  }

}
