package org.pitest.mutationtest;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.PitError;
import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.execute.CoveragePipe;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.extension.TestUnit;
import org.pitest.functional.SideEffect1;
import org.pitest.internal.IsolationUtils;
import org.pitest.util.Log;
import org.pitest.util.Unchecked;

public class CoverageReceiverThread extends Thread {

  private final static Logger               LOG = Log.getLogger();

  private final SideEffect1<CoverageResult> handler;
  private final List<TestUnit>              tus;
  private final int                         port;

  public CoverageReceiverThread(final int port, final List<TestUnit> tus,
      final SideEffect1<CoverageResult> handler) {
    this.setDaemon(true);
    this.handler = handler;
    this.tus = tus;
    this.port = port;
  }

  @Override
  public void run() {

    ServerSocket socket = null;
    Socket clientSocket = null;
    try {
      socket = new ServerSocket(this.port);
      clientSocket = socket.accept();
      final BufferedInputStream bif = new BufferedInputStream(
          clientSocket.getInputStream());

      sendTests(clientSocket);

      final DataInputStream is = new DataInputStream(bif);
      receiveCoverage(is);

    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    } finally {
      try {
        if (clientSocket != null) {
          clientSocket.close();
        }

        if (socket != null) {
          socket.close();
        }
      } catch (final IOException e) {
        throw Unchecked.translateCheckedException(e);
      }
    }

  }

  private void receiveCoverage(final DataInputStream is) throws IOException {
    Description d = null;
    final CoverageStatistics cs = new CoverageStatistics();

    byte control = is.readByte();
    while (control != CoveragePipe.DONE) {
      switch (control) {
      case CoveragePipe.CLAZZ:

        final int id = is.readInt();
        final String name = is.readUTF();

        final int newId = cs.registerClass(name);
        if (id != newId) {
          throw new PitError("Coverage id out of sync");
        }

        break;
      case CoveragePipe.LINE:

        final int classId = is.readInt();
        final int lineId = is.readInt();

        cs.visitLine(classId, lineId);

        break;
      case CoveragePipe.OUTCOME:

        final boolean isGreen = is.readBoolean();
        final long executionTime = is.readLong();
        final CoverageResult cr = new CoverageResult(d, executionTime, isGreen,
            cs.getClassStatistics());

        this.handler.apply(cr);

        cs.clearCoverageStats();

        break;
      case CoveragePipe.TEST_CHANGE:

        final int index = is.readInt();
        d = this.tus.get(index).getDescription();
        break;
      case CoveragePipe.DONE:

      }
      control = is.readByte();
    }
  }

  private void sendTests(final Socket clientSocket) throws IOException {
    final OutputStream os = clientSocket.getOutputStream();
    final Writer w = new BufferedWriter(new OutputStreamWriter(os));
    // send individually to reduce memory overhead of deserializing large suite
    for (final TestUnit tu : this.tus) {
      w.write(IsolationUtils.toTransportString(tu) + "\n");
    }
    w.write("END\n");
    w.flush();
    LOG.info("Sent tests to slave");

  }

  public void waitToFinish() throws InterruptedException {
    this.join();
  }

}
