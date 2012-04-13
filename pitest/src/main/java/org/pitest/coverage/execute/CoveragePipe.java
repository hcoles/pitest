package org.pitest.coverage.execute;

import java.io.DataOutputStream;

import org.pitest.Description;
import org.pitest.coverage.CoverageReceiver;
import org.pitest.mutationtest.instrument.protocol.Id;
import org.pitest.util.ExitCode;
import org.pitest.util.SafeDataOutputStream;

public class CoveragePipe implements CoverageReceiver {

  private final SafeDataOutputStream dos;
  private final HitCache             cache = new HitCache();

  public CoveragePipe(final DataOutputStream dos) {
    this.dos = new SafeDataOutputStream(dos);
  }

  public synchronized void addCodelineInvoke(final int classId,
      final int lineNumber) {

    this.cache.add(classId, lineNumber);

  }

  public synchronized void newTest() {
    this.cache.reset();
  }

  public void recordTestOutcome(Description description, boolean wasGreen,
      long executionTime) {


    this.dos.writeByte(Id.OUTCOME);
    this.dos.write(description);
    this.dos.writeLong(cache.size());
    for ( Long each : cache.values() ) {
      this.dos.writeLong(each);
    }
    this.dos.writeBoolean(wasGreen);
    this.dos.writeLong(executionTime);
 
  }

  public synchronized void end() {
    this.dos.writeByte(Id.DONE);
    this.dos.writeInt(ExitCode.OK.getCode());
    this.dos.flush();
  }

  public synchronized void registerClass(final int id, final String className) {

    this.dos.writeByte(Id.CLAZZ);
    this.dos.writeInt(id);
    this.dos.writeString(className);

  }


}
