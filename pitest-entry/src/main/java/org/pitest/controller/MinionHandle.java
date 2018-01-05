package org.pitest.controller;

public class MinionHandle {
  
  private final Process process;
  
  public MinionHandle(Process process) {
    this.process = process;
  }
  
  public boolean isAlive() {
    // can use process.isAlive from 1.8 onwards
    try {
      int exitVal = process.exitValue();
      return false;
    } catch (IllegalThreadStateException ex) {
      return true;
    }
  }
  
  public void kill() {
    // destroy forcibly not available until java 8
    process.destroy();
  }

}
