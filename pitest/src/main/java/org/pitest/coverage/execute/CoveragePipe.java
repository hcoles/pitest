package org.pitest.coverage.execute;

import java.io.OutputStream;
import java.util.Collection;


import org.pitest.coverage.CoverageReceiver;
import org.pitest.testapi.Description;
import org.pitest.util.ExitCode;
import org.pitest.util.Id;
import org.pitest.util.SafeDataOutputStream;

import sun.pitest.CodeCoverageStore;

public class CoveragePipe implements CoverageReceiver {

  private final SafeDataOutputStream dos;

  public CoveragePipe(final OutputStream dos) {
    this.dos = new SafeDataOutputStream(dos);
  }

  public synchronized void newTest() {
    CodeCoverageStore.reset();
  }

  public synchronized void recordTestOutcome(final Description description,
      final boolean wasGreen, final int executionTime) {
    final Collection<Long> hits = CodeCoverageStore.getHits();

    this.dos.writeByte(Id.OUTCOME);
    this.dos.write(description);
    this.dos.writeLong(hits.size());
    for (final Long each : hits) {
      this.dos.writeLong(each);
    }
    this.dos.writeBoolean(wasGreen);
    this.dos.writeInt(executionTime);

  }

  public synchronized void end(final ExitCode exitCode) {
    this.dos.writeByte(Id.DONE);
    this.dos.writeInt(exitCode.getCode());
    this.dos.flush();
  }

  public synchronized void registerClass(final int id, final String className) {

    this.dos.writeByte(Id.CLAZZ);
    this.dos.writeInt(id);
    this.dos.writeString(className);

  }

}
