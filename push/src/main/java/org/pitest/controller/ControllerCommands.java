package org.pitest.controller;

import org.pitest.minion.ControllerCommandsMXBean;
import org.pitest.minion.commands.Command;
import org.pitest.minion.commands.Status;

public class ControllerCommands implements ControllerCommandsMXBean {
  
  private final MinionPool pool;
  
  public ControllerCommands(MinionPool pool) {
    this.pool = pool;
  }

  @Override
  public void hello(String name) {
    pool.join(name);    
  }

  @Override
  public Command pull(String name) {
    return pool.next(name);
  }

  //@Override
  //public void goodbye(String name) {
  //  pool.unassignMinion(name);
 // }

  @Override
  public void report(String name, Status status) {
    pool.report(name, status);    
  }

}
