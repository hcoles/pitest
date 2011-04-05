package org.pitest.mutationtest;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.pitest.Description;
import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.execute.CoveragePipe;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.functional.FunctionalList;
import org.pitest.functional.MutableList;
import org.pitest.internal.IsolationUtils;
import org.pitest.util.Unchecked;

public class CoverageReceiverThread extends Thread {

  private final FunctionalList<CoverageResult> crs = new MutableList<CoverageResult>();

  public FunctionalList<CoverageResult> getCrs() {
    return this.crs;
  }

  public CoverageReceiverThread() {
    this.setDaemon(true);
  }

  @Override
  public void run() {

    ServerSocket socket = null;
    try {
      socket = new ServerSocket(8187);
      final Socket clientSocket = socket.accept();
      BufferedInputStream bif = new BufferedInputStream(
          clientSocket.getInputStream());
      final DataInputStream is = new DataInputStream(bif);

      // final Map<Integer, String> classes = new HashMap<Integer, String>();
      // Map<Integer, ClassStatistics> coverage = new HashMap<Integer,
      // ClassStatistics>();

      Description d = null;
      final CoverageStatistics cs = new CoverageStatistics();

      byte control = is.readByte();
      while (control != CoveragePipe.DONE) {
        switch (control) {
        case CoveragePipe.CLAZZ:
          final int id = is.readInt();
          final String name = is.readUTF();
          // classes.put(id, name);

          cs.registerClass(name);

          break;
        case CoveragePipe.LINE:

          final int classId = is.readInt();
          final int lineId = is.readInt();

          cs.visitLine(classId, lineId);

          break;
        case CoveragePipe.OUTCOME:

          final boolean isGreen = is.readBoolean();
          final long executionTime = is.readLong();
          final CoverageResult cr = new CoverageResult(d, executionTime,
              isGreen, cs.getClassStatistics());

          this.crs.add(cr);

          cs.clearCoverageStats();

          break;
        case CoveragePipe.TEST_CHANGE:
          d = (Description) IsolationUtils.fromTransportString(is.readUTF());
          break;
        case CoveragePipe.DONE:
          System.out.println("Done");
        }
        control = is.readByte();
      }

    } catch (final IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    } finally {
      if (socket != null) {
        try {
          socket.close();
        } catch (final IOException e) {
          throw Unchecked.translateCheckedException(e);
        }
      }
    }

  }

  public void waitToFinish() throws InterruptedException {
    this.join();
  }

}
