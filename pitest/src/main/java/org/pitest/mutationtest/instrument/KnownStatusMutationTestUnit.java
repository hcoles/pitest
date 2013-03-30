package org.pitest.mutationtest.instrument;

import java.util.Collection;
import java.util.logging.Logger;

import org.pitest.Description;
import org.pitest.MetaData;
import org.pitest.extension.ResultCollector;
import org.pitest.mutationtest.MutationAnalysisUnit;
import org.pitest.mutationtest.results.MutationResult;
import org.pitest.testunit.AbstractTestUnit;
import org.pitest.util.Log;

public class KnownStatusMutationTestUnit extends AbstractTestUnit implements
    MutationAnalysisUnit {

  private static final Logger              LOG = Log.getLogger();

  private final Collection<MutationResult> mutations;
  private final Collection<String>         mutatorNames;

  public KnownStatusMutationTestUnit(final Collection<String> mutatorNames,
      final Collection<MutationResult> mutations) {
    super(new Description("Mutation test"));
    this.mutations = mutations;
    this.mutatorNames = mutatorNames;
  }

  @Override
  public void execute(final ClassLoader loader, final ResultCollector rc) {
    try {
      LOG.fine("Using historic results for " + this.mutations.size()
          + " mutations");
      rc.notifyStart(this.getDescription());
      reportResults(rc);
    } catch (final Throwable ex) {
      rc.notifyEnd(this.getDescription(), ex);
    }

  }

  private void reportResults(final ResultCollector rc) {
    final MetaData md = new MutationMetaData(this.mutatorNames, this.mutations);
    rc.notifyEnd(this.getDescription(), md);
  }

  public int priority() {
    return Integer.MAX_VALUE;
  }

}
