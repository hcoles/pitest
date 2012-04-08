package org.pitest.mutationtest.execute;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;

import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.MutationStatusMap;
import org.pitest.mutationtest.instrument.MutationTestCommunicationThread;
import org.pitest.util.ExitCode;
import org.pitest.util.ProcessArgs;
import org.pitest.util.WrappingProcess;

public class MutationTestProcess {

  private final WrappingProcess                 process;
  private final MutationTestCommunicationThread thread;

  public MutationTestProcess(final int port, final ProcessArgs processArgs,
      final SlaveArguments arguments) {
    this.process = new WrappingProcess(port, processArgs,
        MutationTestSlave.class);
    this.thread = new MutationTestCommunicationThread(port, arguments,
        new HashMap<MutationIdentifier, MutationStatusTestPair>());

  }

  public void start() throws IOException {
    this.thread.start();
    this.process.start();
  }

  public void results(final MutationStatusMap allmutations)
      throws FileNotFoundException, IOException {

    for (final MutationDetails each : allmutations.allMutations()) {
      final MutationStatusTestPair status = this.thread.getStatus(each.getId());
      if (status != null) {
        allmutations.setStatusForMutation(each, status);
      }
    }

  }

  public ExitCode waitToDie() throws InterruptedException {
    this.thread.waitToFinish();
    this.process.destroy();
    return this.thread.getExitCode();
  }

}
