package org.pitest.minion.commands;

import java.beans.ConstructorProperties;

public class Command {
  private final MutId id;
  private final String test;
  private final Action action;
  
  @ConstructorProperties({"id", "test", "action"}) 
  public Command(MutId id, String test, Action action) {
    this.id = id;
    this.test = test;
    this.action = action;
  }
  
  public static Command die() {
    return new Command(null,null, Action.DIE);
  }
  
  public MutId getId() {
    return id;
  }
  
  public String getTest() {
    return test;
  }
  
  public Action getAction() {
    return action;
  }

  @Override
  public String toString() {
    return "Command [id=" + id + ", test=" + test + ", action=" + action + "]";
  }
  
  
  
}
