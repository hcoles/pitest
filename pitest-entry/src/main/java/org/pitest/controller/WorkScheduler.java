package org.pitest.controller;

import org.pitest.minion.commands.Command;
import org.pitest.minion.commands.Status;

public interface WorkScheduler {

  Command next(String worker);
  
  void done(String worker, Command c, Status result);
  
  void awaitCompletion();
  
}
