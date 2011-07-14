package org.pitest.mutationtest.instrument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

import org.pitest.mutationtest.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.util.InputStreamLineIterable;
import org.pitest.util.SafeDataOutputStream;
import org.pitest.util.Unchecked;
import org.pitest.util.WrappingProcess;

class MutationTestProcess extends WrappingProcess {

  private final File           output;
  private final SlaveArguments args;
  private ServerSocket         socket;

  protected MutationTestProcess(final int port, final Args processArgs,
      final SlaveArguments arguments) {

    super(port, processArgs, InstrumentedMutationTestSlave.class);
    this.args = arguments;
    this.output = new File(arguments.outputFileName);

  }

  @Override
  public void start() throws IOException {
    super.start();
    this.socket = new ServerSocket(this.port);
    final Socket clientSocket = this.socket.accept();
    final OutputStream os = clientSocket.getOutputStream();
    final SafeDataOutputStream dos = new SafeDataOutputStream(os);
    dos.write(this.args);
    dos.flush();
    dos.close();

  }

  protected void results(
      final Map<MutationDetails, DetectionStatus> allmutations)
      throws FileNotFoundException, IOException {

    final FileReader fr = new FileReader(this.output);
    final Map<MutationIdentifier, DetectionStatus> idMap = new HashMap<MutationIdentifier, DetectionStatus>();
    final ResultsReader rr = new ResultsReader(idMap);
    try {
      final InputStreamLineIterable li = new InputStreamLineIterable(fr);
      li.forEach(rr);
    } finally {
      fr.close();
    }

    for (final MutationDetails each : allmutations.keySet()) {
      final DetectionStatus status = idMap.get(each.getId());
      if (status != null) {
        allmutations.put(each, status);
      }
    }

  }

  @Override
  public void cleanUp() {
    super.cleanUp();
    this.output.delete();
    if (this.socket != null) {
      try {
        this.socket.close();
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }
  }

}
