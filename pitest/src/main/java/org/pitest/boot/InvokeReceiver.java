package org.pitest.boot;

public interface InvokeReceiver {
  public abstract void addCodelineInvoke(final int classId, final int lineNumber);

  public abstract void registerClass(int id, String className);

}
