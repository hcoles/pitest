package org.pitest.util;

import static org.pitest.functional.Prelude.print;
import static org.pitest.functional.Prelude.printTo;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.pitest.functional.Option;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.ClassPath;
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

  protected final int    port;
  private final Args     argsBuilder;
  private final Class<?> slaveClass;

  private JavaProcess    process;

  public WrappingProcess(final int port, final Args args,
      final Class<?> slaveClass) {
    this.port = port;
    this.argsBuilder = args;
    this.slaveClass = slaveClass;
  }

  public void start() throws IOException {
    final String[] args = { "" + this.port };
    this.process = JavaProcess.launch(this.argsBuilder.stdout,
        this.argsBuilder.stdErr, this.argsBuilder.jvmArgs, this.slaveClass,
        Arrays.asList(args), this.argsBuilder.javaAgentFinder,
        getLaunchClassPath(this.argsBuilder.classPath));
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

  }

}
