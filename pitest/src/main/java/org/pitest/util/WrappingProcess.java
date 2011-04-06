package org.pitest.util;

import static org.pitest.functional.Prelude.print;
import static org.pitest.functional.Prelude.printTo;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
import org.pitest.internal.IsolationUtils;
import org.pitest.internal.classloader.ClassPathRoot;
import org.pitest.mutationtest.instrument.JavaAgentJarFinder;

public abstract class WrappingProcess {

  public static class Args {
    private final ClassPath     classPath;
    private SideEffect1<String> stdout          = print(String.class);
    private SideEffect1<String> stdErr          = printTo(String.class,
                                                    System.err);
    private List<String>        jvmArgs         = Collections.emptyList();
    private JavaAgent           javaAgentFinder = new JavaAgentJarFinder();

    private Args(final ClassPath cp) {
      this.classPath = cp;
    }

    public static Args withClassPath(final ClassPath cp) {
      return new Args(cp);
    }

    public Args andStdout(final SideEffect1<String> stdout) {
      this.stdout = stdout;
      return this;
    }

    public Args andStderr(final SideEffect1<String> stderr) {
      this.stdErr = stderr;
      return this;
    }

    public Args andJVMArgs(final List<String> jvmArgs) {
      this.jvmArgs = jvmArgs;
      return this;
    }

    public Args andJavaAgentFinder(final JavaAgent agent) {
      this.javaAgentFinder = agent;
      return this;
    }
  };

  private final File        input;

  private final JavaProcess process;

  public WrappingProcess(final Args argsBuilder, final Object arguments,
      final Class<?> slaveClass) throws IOException {
    this.input = File.createTempFile(randomFilename(), ".data");

    writeArguments(arguments);

    final String[] args = { this.input.getAbsolutePath() };
    this.process = JavaProcess.launch(argsBuilder.stdout, argsBuilder.stdErr,
        argsBuilder.jvmArgs, slaveClass, Arrays.asList(args),
        argsBuilder.javaAgentFinder, getLaunchClassPath(argsBuilder.classPath));
  }

  private void writeArguments(final Object arguments) throws IOException {
    final BufferedWriter bw = new BufferedWriter(new FileWriter(this.input));
    bw.append(IsolationUtils.toTransportString(arguments));
    bw.close();
  }

  public static String randomFilename() {
    return System.currentTimeMillis()
        + ("" + Math.random()).replaceAll("\\.", "");
  }

  private String getLaunchClassPath(final ClassPath cp) {
    StringBuilder classpath = new StringBuilder(
        System.getProperty("java.class.path"));
    for (final ClassPathRoot each : cp) {
      final Option<String> additional = each.cacheLocation();
      for (final String path : additional) {
        classpath = classpath.append(File.pathSeparator + path);
      }
    }

    return classpath.toString();
  }

  public int waitToDie() throws InterruptedException {
    return this.process.waitToDie();
  }

  public void cleanUp() {
    this.input.delete();
  }

}
