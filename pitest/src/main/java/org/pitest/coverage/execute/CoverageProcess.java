package org.pitest.coverage.execute;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import org.pitest.coverage.CoverageResult;
import org.pitest.functional.SideEffect1;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.ExitCode;

public class CoverageProcess {

  private final WrappingProcess             process;
  private final CoverageCommunicationThread crt;

  public CoverageProcess(final ProcessArgs processArgs,
      final CoverageOptions arguments, final ServerSocket socket,
      final List<String> testClases, final SideEffect1<CoverageResult> handler)
          throws IOException {
    this.process = new WrappingProcess(socket.getLocalPort(), processArgs,
        CoverageMinion.class);
    this.crt = new CoverageCommunicationThread(socket, arguments, testClases,
        handler);
  }

  public void start() throws IOException, InterruptedException {
    this.crt.start();
    this.process.start();
  }

  public ExitCode waitToDie() throws InterruptedException {
    try {
      return this.crt.waitToFinish();
    } finally {
      this.process.destroy();
    }

  }

}
