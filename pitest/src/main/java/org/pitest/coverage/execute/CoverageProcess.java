package org.pitest.coverage.execute;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.pitest.functional.F;
import org.pitest.functional.FunctionalIterable;
import org.pitest.internal.IsolationUtils;
import org.pitest.util.InputStreamLineIterable;
import org.pitest.util.WrappingProcess;

public class CoverageProcess extends WrappingProcess {

  public CoverageProcess(final Args processArgs, final SlaveArguments arguments)
      throws IOException {
    super(processArgs, arguments, CoverageSlave.class);
  }

  public FunctionalIterable<CoverageResult> results()
      throws FileNotFoundException, IOException {

    final FileReader fr = new FileReader(this.getOutputFile());
    try {
      final InputStreamLineIterable li = new InputStreamLineIterable(fr);
      return li.map(stringToCoverageResult());
    } finally {
      fr.close();
    }

  }

  private F<String, CoverageResult> stringToCoverageResult() {
    return new F<String, CoverageResult>() {

      public CoverageResult apply(final String a) {
        return (CoverageResult) IsolationUtils.fromTransportString(a);
      }

    };
  }

}
