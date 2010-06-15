package org.pitest.distributed.slave;

import org.pitest.distributed.message.RunDetails;

public interface SlaveService {

  public void executeTest(RunDetails run, byte[] testGroup);

}
