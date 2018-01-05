package org.pitest.controller;

import org.pitest.minion.commands.Command;

public final class Work {

  private final Command command;
  private final int maxDurationMs;
  
  public Work(Command command, int maxDurationMs) {
    this.command = command;
    this.maxDurationMs = maxDurationMs;
  }
  
  
  public static Work untimed(Command c) {
    // FIXME
    return new Work(c, 100);
  }


  public static Work of(int time, Command c) {
    return new Work(c, time);
  }
  
  public Command command() {
    return command;
  }
  
  public int duration() {
    return this.maxDurationMs;
  }
  
}
