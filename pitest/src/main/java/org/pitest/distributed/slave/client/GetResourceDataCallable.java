package org.pitest.distributed.slave.client;

import static org.pitest.util.Unchecked.translateCheckedException;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.concurrent.Callable;

import org.pitest.distributed.DistributedContainer;
import org.pitest.distributed.message.RunDetails;
import org.pitest.functional.Option;
import org.pitest.internal.ClassPath;

public class GetResourceDataCallable implements Callable<Option<byte[]>>,
    Serializable {

  private static final long serialVersionUID = 1L;

  private final RunDetails  run;
  private final String      name;

  public GetResourceDataCallable(final RunDetails run, final String name) {
    this.run = run;
    this.name = name;
  }

  public Option<byte[]> call() throws Exception {
    final DistributedContainer container = DistributedContainer
        .getInstanceForRun(this.run);
    final URL url = container.getClassPath().findResource(this.name);
    try {
      if (url != null) {
        return Option.some(ClassPath.streamToByteArray(url.openStream()));

      } else {
        return Option.none();
      }
    } catch (final IOException ex) {
      throw translateCheckedException(ex);
    }

  }

}
