package org.pitest.mutationtest.instrument;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;

import org.pitest.functional.Option;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.instrument.ResultsReader.DetectionStatus;
import org.pitest.util.InputStreamLineIterable;
import org.pitest.util.WrappingProcess;

class MutationTestProcess extends WrappingProcess {

  private final File output;

  protected MutationTestProcess(final Args processArgs,
      final SlaveArguments arguments) throws IOException {
    super(processArgs, arguments, InstrumentedMutationTestSlave.class);
    this.output = new File(arguments.outputFileName);

  }

  protected Option<Statistics> results(
      final Map<MutationIdentifier, DetectionStatus> allmutations,
      final Option<Statistics> stats) throws FileNotFoundException, IOException {

    final FileReader fr = new FileReader(this.output);
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
