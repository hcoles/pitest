package org.pitest.coverage.execute;

import java.io.IOException;
import java.util.List;

import org.pitest.extension.TestUnit;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.CoverageCommunicationThread;
import org.pitest.util.ProcessArgs;
import org.pitest.util.WrappingProcess;

public class CoverageProcess {

  private final WrappingProcess             process;
  private final CoverageCommunicationThread crt;

  public CoverageProcess(final ProcessArgs processArgs,
      final SlaveArguments arguments, final int port, final List<TestUnit> tus,
      final SideEffect1<CoverageResult> handler) throws IOException {
    this.process = new WrappingProcess(port, processArgs, CoverageSlave.class);
    this.crt = new CoverageCommunicationThread(port, arguments, tus, handler);
  }

  public void start() throws IOException {
    this.process.start();
    this.crt.start();
  }

  public int waitToDie() throws InterruptedException {
    final int exitCode = this.process.waitToDie();
    this.crt.waitToFinish();
    return exitCode;
  }

}
