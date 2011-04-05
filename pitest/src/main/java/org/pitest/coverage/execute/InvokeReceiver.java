package org.pitest.coverage.execute;

import java.util.Collection;

import org.pitest.coverage.InvokeEntry;

public interface InvokeReceiver {
  public abstract void addCodelineInvoke(final int classId, final int lineNumber);

  public abstract boolean isEmpty();

  public abstract Collection<? extends InvokeEntry> poll(int i)
      throws InterruptedException;

  public abstract void registerClass(int id, String className);

}
