package org.pitest.util;

public interface Monitor {

  public abstract void requestStop();

  public abstract void requestStart();

  public abstract void waitForExit(long timeoutInMs);

}