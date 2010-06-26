package org.pitest.distributed;

import org.pitest.distributed.message.RunDetails;

public class ControlMessage {

  public enum Type {
    RUN_COMPLETE;
  };

  ControlMessage(final Type type, final RunDetails run) {
    this.type = type;
    this.run = run;
  }

  public Type getType() {
    return this.type;
  }

  private final Type       type;
  private final RunDetails run;

  public RunDetails getRun() {
    return this.run;
  }

}
