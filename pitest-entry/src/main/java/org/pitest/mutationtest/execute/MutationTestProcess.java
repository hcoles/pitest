package org.pitest.mutationtest.execute;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Optional;

import org.pitest.mutationtest.MutationStatusMap;
import org.pitest.mutationtest.MutationStatusTestPair;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.ExitCode;

public class MutationTestProcess {

  private final WrappingProcess                 process;
  private final MutationTestCommunicationThread thread;

  public MutationTestProcess(final ServerSocket socket,
      final ProcessArgs processArgs, final MinionArguments arguments) {
    this.process = new WrappingProcess(socket.getLocalPort(), processArgs,
        MutationTestMinion.class);
    this.thread = new MutationTestCommunicationThread(socket, arguments,
            new HashMap<>());

  }

  public void start() throws IOException, InterruptedException {
    this.thread.start();
    this.process.start();
  }

  public void results(final MutationStatusMap allmutations) throws IOException {

    for (final MutationDetails each : allmutations.allMutations()) {
      final MutationStatusTestPair status = this.thread.getStatus(each.getId());
      if (status != null) {
        allmutations.setStatusForMutation(each, status);
      }
    }

  }

  public ExitCode waitToDie() {
    try {
      Optional<ExitCode> maybeExit = this.thread.waitToFinish(5);
      while (!maybeExit.isPresent() && this.process.isAlive()) {
        maybeExit = this.thread.waitToFinish(10);
      }
      return maybeExit.orElse(ExitCode.MINION_DIED);
    } finally {
      this.process.destroy();
    }

  }

}
