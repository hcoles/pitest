package org.pitest.mutationtest.instrument;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.util.InputStreamLineIterable;
import org.pitest.util.WrappingProcess;

public class MutationTestProcess extends WrappingProcess {

  public MutationTestProcess(final Args processArgs,
      final SlaveArguments arguments) throws IOException {
    super(processArgs, arguments, InstrumentedMutationTestSlave.class);

  }

  public Option<Statistics> results(
      final Map<MutationIdentifier, DetectionStatus> allmutations,
      final Option<Statistics> stats) throws FileNotFoundException, IOException {

    final FileReader fr = new FileReader(this.getOutputFile());
    final ResultsReader rr = new ResultsReader(allmutations, stats);
    try {
      final InputStreamLineIterable li = new InputStreamLineIterable(fr);
      li.forEach(rr);
    } finally {
      fr.close();
    }

    return rr.getStats();
  }

}
