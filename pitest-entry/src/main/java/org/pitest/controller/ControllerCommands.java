package org.pitest.controller;

import org.pitest.minion.ControllerCommandsMXBean;
import org.pitest.minion.commands.Command;
import org.pitest.minion.commands.MinionConfig;
import org.pitest.minion.commands.Status;

public class ControllerCommands implements ControllerCommandsMXBean {
  
  private final MinionPool pool;
  private final MinionConfig minionConf;
  
  public ControllerCommands(MinionPool pool, MinionConfig minionConf) {
    this.pool = pool;
    this.minionConf = minionConf;
  }

  @Override
  public MinionConfig hello(String name) {
    pool.join(name);
    return minionConf;
  }

  @Override
  public Command pull(String name) {
    return pool.next(name);
  }


  @Override
  public void report(String name, Status status) {
    pool.report(name, status);    
  }

}
