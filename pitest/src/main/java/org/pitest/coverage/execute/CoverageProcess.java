package org.pitest.coverage.execute;

import java.io.IOException;

import org.pitest.util.WrappingProcess;

public class CoverageProcess extends WrappingProcess {

  public CoverageProcess(final Args processArgs, final SlaveArguments arguments)
      throws IOException {
    super(processArgs, arguments, CoverageSlave.class);
  }

}
