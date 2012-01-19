package org.pitest.coverage.execute;

import java.io.DataOutputStream;

import org.pitest.Description;
import org.pitest.coverage.CoverageReceiver;
import org.pitest.mutationtest.instrument.protocol.Id;
import org.pitest.util.SafeDataOutputStream;

public class CoveragePipe implements CoverageReceiver {

  private final SafeDataOutputStream dos;
  private final HitCache             cache = new HitCache();

  public CoveragePipe(final DataOutputStream dos) {
    this.dos = new SafeDataOutputStream(dos);
  }

  public synchronized void addCodelineInvoke(final int classId,
      final int lineNumber) {

    if (!this.cache.checkHit(classId, lineNumber)) {
      this.dos.writeByte(Id.LINE);
      this.dos.writeInt(classId);
      this.dos.writeInt(lineNumber);
    }

  }

  public synchronized void recordTest(final Description description) {

    this.cache.reset();
    this.dos.writeByte(Id.TEST_CHANGE);
    this.dos.write(description);

  }

  public synchronized void recordTestOutcome(final boolean wasGreen,
      final long executionTime) {

    this.dos.writeByte(Id.OUTCOME);
    this.dos.writeBoolean(wasGreen);
    this.dos.writeLong(executionTime);

  }

  public synchronized void end() {

    this.dos.writeByte(Id.DONE);
    this.dos.flush();

  }

  public synchronized void registerClass(final int id, final String className) {

    this.dos.writeByte(Id.CLAZZ);
    this.dos.writeInt(id);
    this.dos.writeString(className);

  }
}
