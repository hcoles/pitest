package org.pitest.coverage.execute;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.pitest.coverage.CoverageReceiver;
import org.pitest.coverage.InvokeEntry;
import org.pitest.util.Unchecked;

public class CoveragePipe implements CoverageReceiver {

  public final static byte       LINE        = 1;
  public final static byte       TEST_CHANGE = 2;
  public final static byte       OUTCOME     = 4;
  public final static byte       CLAZZ       = 8;
  public final static byte       DONE        = 16;

  private final DataOutputStream dos;

  public CoveragePipe(final DataOutputStream dos) {
    this.dos = dos;
  }

  public synchronized void addCodelineInvoke(final int classId,
      final int lineNumber) {
    try {
      this.dos.writeByte(LINE);
      this.dos.writeInt(classId);
      this.dos.writeInt(lineNumber);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }

  }

  public synchronized void recordTest(final int testIndex) {
    try {
      this.dos.writeByte(TEST_CHANGE);
      this.dos.writeInt(testIndex);
      ;
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public synchronized void recordTestOutcome(final boolean wasGreen,
      final long executionTime) {
    try {
      this.dos.writeByte(OUTCOME);
      this.dos.writeBoolean(wasGreen);
      this.dos.writeLong(executionTime);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);

    }

  }

  public synchronized void end() {
    try {
      this.dos.writeByte(DONE);
      this.dos.flush();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);

    }
  }

  public boolean isEmpty() {
    return false;
  }

  public Collection<? extends InvokeEntry> poll(final int i)
      throws InterruptedException {
    return null;
  }

  public synchronized void registerClass(final int id, final String className) {
    try {
      this.dos.writeByte(CLAZZ);
      this.dos.writeInt(id);
      this.dos.writeUTF(className);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);

    }
  }
}
