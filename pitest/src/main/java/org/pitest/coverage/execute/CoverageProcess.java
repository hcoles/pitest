package org.pitest.coverage.execute;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.pitest.functional.F;
import org.pitest.functional.FunctionalList;
import org.pitest.internal.IsolationUtils;
import org.pitest.util.InputStreamLineIterable;
import org.pitest.util.WrappingProcess;

public class CoverageProcess extends WrappingProcess {

  public CoverageProcess(final Args processArgs, final SlaveArguments arguments)
      throws IOException {
    super(processArgs, arguments, CoverageSlave.class);
  }

  public FunctionalList<CoverageResult> results() throws FileNotFoundException,
      IOException {

    final FileReader fr = new FileReader(this.getOutputFile());
    try {
      final InputStreamLineIterable li = new InputStreamLineIterable(fr);
      return li.flatMap(stringToCoverageResult());
    } finally {
      fr.close();
    }

  }

  private F<String, List<CoverageResult>> stringToCoverageResult() {
    return new F<String, List<CoverageResult>>() {

      @SuppressWarnings("unchecked")
      public List<CoverageResult> apply(final String a) {
        return (List<CoverageResult>) IsolationUtils.fromXml(a);

      }

    };
  }

}
