package org.pitest.distributed.slave.client;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.Callable;

import org.pitest.distributed.DistributedContainer;
import org.pitest.distributed.message.RunDetails;

public class GetEnvironmentCallable implements Callable<Map<String, String>>,
    Serializable {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;
  private final RunDetails  run;

  public GetEnvironmentCallable(final RunDetails run) {
    this.run = run;

  }

  public Map<String, String> call() throws Exception {

    final DistributedContainer container = DistributedContainer
        .getInstanceForRun(this.run);
    return container.getEnvironment();

  }

}
