package org.pitest.mutationtest.tdg.execute;

import java.net.ServerSocket;
import java.util.function.Consumer;
import java.util.List;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.ExitCode;
import java.util.Optional;
import java.io.IOException;
import org.pitest.coverage.execute.TdgMinion;
import org.pitest.coverage.execute.CoverageMinion;
public class TdgProcess {
    private final WrappingProcess             process;
    private final TdgCommunicationThread crt;
    public TdgProcess(final ProcessArgs processArgs, final ServerSocket socket, final List<String> testClases,
    final Consumer<TdgResult> handler) {
        this.process = new WrappingProcess(socket.getLocalPort(), processArgs,
        TdgMinion.class);
        this.crt = new TdgCommunicationThread(socket, testClases,
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
            System.out.println("waiting minion exit normally...");  
            maybeExit = this.crt.waitToFinish(10);
          }
          return maybeExit.orElse(ExitCode.MINION_DIED);
        } finally {
          this.process.destroy();
        }
    
    }
}
