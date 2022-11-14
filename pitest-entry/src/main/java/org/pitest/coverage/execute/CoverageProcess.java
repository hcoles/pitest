package org.pitest.coverage.execute;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.pitest.coverage.CoverageResult;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.ExitCode;

public class CoverageProcess {

  private final WrappingProcess             process;
  private final CoverageCommunicationThread crt;

  public CoverageProcess(final ProcessArgs processArgs,
      final CoverageOptions arguments, final ServerSocket socket,
      final List<String> testClasses, final Consumer<CoverageResult> handler) {
    this.process = new WrappingProcess(socket.getLocalPort(), processArgs,
        CoverageMinion.class);
    this.crt = new CoverageCommunicationThread(socket, arguments, testClasses,
        handler);
  }

  public void start() throws IOException, InterruptedException {
    this.crt.start();
    this.process.start();
  }

  public ExitCode waitToDie() {
    try {
      Optional<ExitCode> maybeExit = this.crt.waitToFinish(5);
      while (!maybeExit.isPresent() && this.process.isAlive()) {
        maybeExit = this.crt.waitToFinish(10);
      }
      return maybeExit.orElse(ExitCode.MINION_DIED);
    } finally {
      this.process.destroy();
    }

  }

}
