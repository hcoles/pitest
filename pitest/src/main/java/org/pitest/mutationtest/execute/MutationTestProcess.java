package org.pitest.mutationtest.execute;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.concurrent.ExecutionException;

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

  public MutationTestProcess(final ServerSocket socket,
      final ProcessArgs processArgs, final SlaveArguments arguments) {
    this.process = new WrappingProcess(socket.getLocalPort(), processArgs,
        MutationTestSlave.class);
    this.thread = new MutationTestCommunicationThread(socket, arguments,
        new HashMap<MutationIdentifier, MutationStatusTestPair>());

  }

  public void start() throws IOException, InterruptedException {
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

  public ExitCode waitToDie() throws InterruptedException, ExecutionException {
    final ExitCode exitCode = this.thread.waitToFinish();
    this.process.destroy();
    return exitCode;
  }

}
