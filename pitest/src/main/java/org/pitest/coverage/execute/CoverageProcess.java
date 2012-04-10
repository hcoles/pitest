package org.pitest.coverage.execute;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.CoverageCommunicationThread;
import org.pitest.util.ProcessArgs;
import org.pitest.util.WrappingProcess;

public class CoverageProcess {

  private final WrappingProcess             process;
  private final CoverageCommunicationThread crt;

  public CoverageProcess(final ProcessArgs processArgs,
      final CoverageOptions arguments, final ServerSocket socket,
      final List<String> testClases, final SideEffect1<CoverageResult> handler)
      throws IOException {
    this.process = new WrappingProcess(socket.getLocalPort(), processArgs, CoverageSlave.class);
    this.crt = new CoverageCommunicationThread(socket, arguments, testClases,
        handler);
  }

  public void start() throws IOException {
    this.crt.start();
    this.process.start();
  }

  public int waitToDie() throws InterruptedException {
    final int exitCode = this.process.waitToDie();
    this.crt.waitToFinish();
    return exitCode;
  }

}
