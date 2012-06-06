package org.pitest.coverage.execute;

import java.io.DataOutputStream;
import java.util.Collection;

import org.pitest.Description;
import org.pitest.boot.CodeCoverageStore;
import org.pitest.coverage.CoverageReceiver;
import org.pitest.mutationtest.instrument.protocol.Id;
import org.pitest.util.ExitCode;
import org.pitest.util.SafeDataOutputStream;

public class CoveragePipe implements CoverageReceiver {

  private final SafeDataOutputStream dos;

  public CoveragePipe(final DataOutputStream dos) {
    this.dos = new SafeDataOutputStream(dos);
  }

  public synchronized void newTest() {
    CodeCoverageStore.reset();
  }

  public synchronized void recordTestOutcome(final Description description,
      final boolean wasGreen, final long executionTime) {
    final Collection<Long> hits = CodeCoverageStore.getHits();

    this.dos.writeByte(Id.OUTCOME);
    this.dos.write(description);
    this.dos.writeLong(hits.size());
    for (final Long each : hits) {
      this.dos.writeLong(each);
    }
    this.dos.writeBoolean(wasGreen);
    this.dos.writeLong(executionTime);

  }

  public synchronized void end(ExitCode exitCode) {
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
