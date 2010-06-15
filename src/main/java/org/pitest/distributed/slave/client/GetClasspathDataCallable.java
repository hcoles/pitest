package org.pitest.distributed.slave.client;

import java.io.Serializable;
import java.util.concurrent.Callable;

import org.pitest.distributed.DistributedContainer;
import org.pitest.distributed.message.RunDetails;

public class GetClasspathDataCallable implements Callable<byte[]>, Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final RunDetails  run;
  private final String      name;

  public GetClasspathDataCallable(final RunDetails run, final String name) {
    this.run = run;
    this.name = name;
  }

  public byte[] call() throws Exception {

    final DistributedContainer container = DistributedContainer
        .getInstanceForRun(this.run);
    return container.getClassPath().getClassData(this.name);

  }

}
