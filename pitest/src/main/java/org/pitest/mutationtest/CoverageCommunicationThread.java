package org.pitest.mutationtest;

import java.util.List;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.PitError;
import org.pitest.coverage.CoverageStatistics;
import org.pitest.coverage.execute.CoverageResult;
import org.pitest.coverage.execute.SlaveArguments;
import org.pitest.functional.SideEffect1;
import org.pitest.mutationtest.instrument.protocol.Id;
import org.pitest.util.CommunicationThread;
import org.pitest.util.Log;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

public class CoverageCommunicationThread extends CommunicationThread {

  static class SendData implements SideEffect1<SafeDataOutputStream> {
    private final static Logger  LOG = Log.getLogger();
    private final SlaveArguments arguments;
    private final List<String>   testClasses;

    SendData(final SlaveArguments arguments, final List<String> testClasses) {
      this.arguments = arguments;
      this.testClasses = testClasses;
    }

    public void apply(final SafeDataOutputStream dos) {
      sendArguments(dos);
      sendTests(dos);
    }

    private void sendArguments(final SafeDataOutputStream dos) {
      dos.write(this.arguments);
      dos.flush();
    }

    private void sendTests(final SafeDataOutputStream dos) {

      // send individually to reduce memory overhead of deserializing large
      // suite
      dos.writeInt(this.testClasses.size());
      for (final String tc : this.testClasses) {
        dos.writeString(tc);
      }
      dos.flush();
      LOG.info("Sent tests to slave");

    }
  }

  static class Receive implements ReceiveStrategy {

    private Description                       d  = null;
    final CoverageStatistics                  cs = new CoverageStatistics();
    private final SideEffect1<CoverageResult> handler;

    Receive(final SideEffect1<CoverageResult> handler) {
      this.handler = handler;
    }

    public void apply(final byte control, final SafeDataInputStream is) {
      switch (control) {
      case Id.CLAZZ:

        final int id = is.readInt();
        final String name = is.readString();

        final int newId = this.cs.registerClass(name);
        if (id != newId) {
          throw new PitError("Coverage id out of sync");
        }

        break;
      case Id.LINE:

        final int classId = is.readInt();
        final int lineId = is.readInt();

        this.cs.visitLine(classId, lineId);

        break;
      case Id.OUTCOME:

        final boolean isGreen = is.readBoolean();
        final long executionTime = is.readLong();
        final CoverageResult cr = new CoverageResult(this.d, executionTime,
            isGreen, this.cs.getClassStatistics());

        this.handler.apply(cr);

        this.cs.clearCoverageStats();

        break;
      case Id.TEST_CHANGE:
        this.d = is.read(Description.class);
        break;
      case Id.DONE:

      }
    }

  }

  public CoverageCommunicationThread(final int port,
      final SlaveArguments arguments, final List<String> tus,
      final SideEffect1<CoverageResult> handler) {
    super(port, new SendData(arguments, tus), new Receive(handler));

  }

}
