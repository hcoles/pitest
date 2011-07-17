package org.pitest.util;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.pitest.functional.Option;
import org.pitest.internal.ClassPath;
import org.pitest.internal.classloader.ClassPathRoot;

public class WrappingProcess {

  protected final int       port;
  private final ProcessArgs argsBuilder;
  private final Class<?>    slaveClass;

  private JavaProcess       process;

  public WrappingProcess(final int port, final ProcessArgs args,
      final Class<?> slaveClass) {
    this.port = port;
    this.argsBuilder = args;
    this.slaveClass = slaveClass;
  }

  public void start() throws IOException {
    final String[] args = { "" + this.port };
    this.process = JavaProcess.launch(this.argsBuilder.getStdout(),
        this.argsBuilder.getStdErr(), this.argsBuilder.getJvmArgs(),
        this.slaveClass, Arrays.asList(args),
        this.argsBuilder.getJavaAgentFinder(),
        getLaunchClassPath(this.argsBuilder.getClassPath()));
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

  public int getPort() {
    return this.port;
  }

}
