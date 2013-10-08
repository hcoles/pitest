package org.pitest.process;

import java.io.IOException;
import java.util.Arrays;

public class WrappingProcess {

  private final int         port;
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
    this.process = JavaProcess.launch(this.argsBuilder.getWorkingDir(),
        this.argsBuilder.getJavaExecutable(), this.argsBuilder.getStdout(),
        this.argsBuilder.getStdErr(), this.argsBuilder.getJvmArgs(),
        this.slaveClass, Arrays.asList(args),
        this.argsBuilder.getJavaAgentFinder(),
        this.argsBuilder.getLaunchClassPath());
  }

  public int waitToDie() throws InterruptedException {
    return this.process.waitToDie();
  }

  public void destroy() {
    this.process.destroy();
  }

}
