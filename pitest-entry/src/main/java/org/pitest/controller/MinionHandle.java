package org.pitest.controller;

public class MinionHandle {
  
  private final Process process;
  
  public MinionHandle(Process process) {
    this.process = process;

  }
  
  public void kill() {
    // destroy forcibly not available until java 8
    process.destroy();
  }

}
