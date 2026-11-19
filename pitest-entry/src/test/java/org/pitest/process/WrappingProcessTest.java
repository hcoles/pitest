package org.pitest.process;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.pitest.classpath.ClassPath;
import org.pitest.mutationtest.tooling.JarCreatingJarFinder;
import org.pitest.util.NullJavaAgent;

public class WrappingProcessTest {

  private static final int EXIT_CODE = 10;

  @Rule
  public TemporaryFolder folder = new TemporaryFolder();

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

    final LaunchOptions launchOptions = new LaunchOptions(NullJavaAgent.instance(),
        new DefaultJavaExecutableLocator(), Collections.emptyList(),
        new HashMap<>());

    final ProcessArgs processArgs = ProcessArgs
        .withClassPath(new ClassPath().getLocalClassPath())
        .andBaseDir(new File(System.getProperty("user.dir")))
        .andLaunchOptions(launchOptions).andStdout(nullHandler())
        .andStderr(nullHandler());

    final WrappingProcess wrappingProcess = WrappingProcess.create(java.nio.file.Path.of("none"), processArgs,
        getClass());
    wrappingProcess.start();
    final JavaProcess process = wrappingProcess.getProcess();

    assertTrue(process.isAlive());
    assertEquals(EXIT_CODE, process.waitToDie());
  }

  @Test
  public void passesJvmArgsToChildUnchanged() throws Exception {
    List<String> jvmArgs = expectedPropertiesAsJvmArgs();

    List<String> output = new CopyOnWriteArrayList<>();
    int exitCode = launch(PropertyChecker.class, jvmArgs, output);
    assertEquals(String.join("\n", output), 0, exitCode);
  }

  @Test
  public void passesLinesOfSuppliedArgFilesThroughAsIs() throws Exception {
    // lines of a user supplied args file are already in args file syntax
    Path argFile = folder.newFile("user.args").toPath();
    Files.write(argFile, Collections.singletonList("\"-Dpit.fromfile=x y\""));

    List<String> jvmArgs = new ArrayList<>();
    jvmArgs.add("@" + argFile.toAbsolutePath());

    List<String> output = new CopyOnWriteArrayList<>();
    int exitCode = launch(ArgFilePropertyChecker.class, jvmArgs, output);
    assertEquals(String.join("\n", output), 0, exitCode);
  }

  @Test
  public void removesJacocoAgentWithSpaceInPath() throws Exception {
    // the child would fail to start if the (non existent) agent were passed through
    List<String> jvmArgs = new ArrayList<>();
    jvmArgs.add("-javaagent:/does not exist/jacocoagent.jar=destfile=a b.exec");

    List<String> output = new CopyOnWriteArrayList<>();
    int exitCode = launch(getClass(), jvmArgs, output);
    assertEquals(String.join("\n", output), EXIT_CODE, exitCode);
  }

  private static List<String> expectedPropertiesAsJvmArgs() {
    List<String> jvmArgs = new ArrayList<>();
    PropertyChecker.EXPECTED.forEach((k, v) -> jvmArgs.add("-D" + k + "=" + v));
    return jvmArgs;
  }

  private int launch(Class<?> minionClass, List<String> jvmArgs, List<String> output)
      throws IOException, InterruptedException {
    // the args file is cached by agent location, so use a fresh agent for each launch
    final JarCreatingJarFinder agent = new JarCreatingJarFinder();
    try {
      final LaunchOptions launchOptions = new LaunchOptions(agent,
          new DefaultJavaExecutableLocator(), jvmArgs, new HashMap<>());

      final ProcessArgs processArgs = ProcessArgs
          .withClassPath(new ClassPath().getLocalClassPath())
          .andBaseDir(new File(System.getProperty("user.dir")))
          .andLaunchOptions(launchOptions).andStdout(output::add)
          .andStderr(output::add);

      final WrappingProcess wrappingProcess = WrappingProcess.create(Path.of("none"), processArgs,
          minionClass);
      wrappingProcess.start();
      return wrappingProcess.getProcess().waitToDie();
    } finally {
      agent.close();
    }
  }

  private Consumer<String> nullHandler() {
    return a -> {

    };
  }

  public static class PropertyChecker {

    static final Map<String, String> EXPECTED = new LinkedHashMap<>();

    static {
      EXPECTED.put("pit.space", "a b");
      EXPECTED.put("pit.tab", "a\tb");
      EXPECTED.put("pit.form.feed", "a\fb");
      EXPECTED.put("pit.hash", "a#b");
      EXPECTED.put("pit.single.quote", "it's");
      EXPECTED.put("pit.double.quote", "say \"hi\"");
      EXPECTED.put("pit.windows.path", "C:\\Program Files\\pit\\");
      EXPECTED.put("pit.backslash.quote", "\\\"");
      EXPECTED.put("pit.newline", "a\nb");
      EXPECTED.put("pit.carriage.return", "a\rb");
    }

    public static void main(final String[] args) {
      System.exit(mismatches(EXPECTED));
    }

    static int mismatches(Map<String, String> expected) {
      int mismatches = 0;
      for (Map.Entry<String, String> each : expected.entrySet()) {
        String actual = System.getProperty(each.getKey());
        if (!each.getValue().equals(actual)) {
          System.err.println(each.getKey() + " was [" + actual + "] not [" + each.getValue() + "]");
          mismatches++;
        }
      }
      return mismatches;
    }
  }

  public static class ArgFilePropertyChecker {
    public static void main(final String[] args) {
      System.exit(PropertyChecker.mismatches(Collections.singletonMap("pit.fromfile", "x y")));
    }
  }

}
