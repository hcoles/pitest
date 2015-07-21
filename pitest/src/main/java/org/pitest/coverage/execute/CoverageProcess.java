package org.pitest.coverage.execute;

import org.pitest.coverage.CoverageResult;
import org.pitest.functional.SideEffect1;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.ExitCode;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.List;

public class CoverageProcess {

  private final WrappingProcess             process;
  private final CoverageCommunicationThread coverageCommunicationThread;

  public CoverageProcess(ProcessArgs processArgs, CoverageOptions arguments,
                         ServerSocket socket, List<String> testClases,
                         SideEffect1<CoverageResult> handler)
      throws IOException {
    this.process = new WrappingProcess(socket.getLocalPort(),
                                       processArgs,
                                       CoverageSlave.class);
    this.coverageCommunicationThread = new CoverageCommunicationThread(socket,
                                               arguments,
                                               testClases,
                                               handler);
  }

  public void start() throws IOException, InterruptedException {
    coverageCommunicationThread.start();
    process.start();
  }

  public ExitCode waitToDie() throws InterruptedException {
    try {
      return coverageCommunicationThread.waitToFinish();
    } finally {
      process.destroy();
    }

  }

}
