package org.pitest.coverage.execute;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Collection;

import org.pitest.Description;
import org.pitest.coverage.CoverageReceiver;
import org.pitest.coverage.InvokeEntry;
import org.pitest.internal.IsolationUtils;
import org.pitest.util.Unchecked;

public class CoveragePipe implements CoverageReceiver {

  public final static byte       LINE        = 00;
  public final static byte       TEST_CHANGE = 01;
  public final static byte       OUTCOME     = 02;
  public final static byte       CLAZZ       = 03;
  public final static byte       DONE        = 04;

  private final DataOutputStream dos;

  public CoveragePipe(final DataOutputStream dos) {
    this.dos = dos;
  }

  public void addCodelineInvoke(final int classId, final int lineNumber) {
    try {
      this.dos.writeByte(LINE);
      this.dos.writeInt(classId);
      this.dos.writeInt(lineNumber);
      this.dos.flush();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }

  }

  public void recordTest(final Description description) {
    try {
      this.dos.writeByte(TEST_CHANGE);
      this.dos.writeUTF(IsolationUtils.toTransportString(description));
      this.dos.flush();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);
    }
  }

  public void recordTestOutcome(final boolean wasGreen, final long executionTime) {
    try {
      this.dos.writeByte(OUTCOME);
      this.dos.writeBoolean(wasGreen);
      this.dos.writeLong(executionTime);
      this.dos.flush();
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);

    }

  }

  public void end() {
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

  public void registerClass(final int id, final String className) {
    try {
      this.dos.writeByte(CLAZZ);
      this.dos.writeInt(id);
      this.dos.writeUTF(className);
    } catch (final IOException e) {
      throw Unchecked.translateCheckedException(e);

    }
  }
}
