package org.pitest.minion;

import org.pitest.minion.commands.Command;
import org.pitest.minion.commands.Status;

public interface ControllerCommandsMXBean {

  void hello(String name);
  
  Command pull(String name);
  
  void goodbye(String name);

  void report(String name, Status status);

    
}
