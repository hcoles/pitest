package org.pitest.mutationtest.build;

import java.util.List;
import java.util.logging.Logger;

import org.pitest.mutationtest.MutationMetaData;
import org.pitest.mutationtest.MutationResult;
import org.pitest.testapi.AbstractTestUnit;
import org.pitest.testapi.Description;
import org.pitest.testapi.MetaData;
import org.pitest.testapi.ResultCollector;
import org.pitest.util.Log;

public class KnownStatusMutationTestUnit extends AbstractTestUnit implements
    MutationAnalysisUnit {

  private static final Logger        LOG = Log.getLogger();

  private final List<MutationResult> mutations;

  public KnownStatusMutationTestUnit(final List<MutationResult> mutations) {
    super(new Description("Mutation test"));
    this.mutations = mutations;
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
    final MetaData md = new MutationMetaData(this.mutations);
    rc.notifyEnd(this.getDescription(), md);
  }

  public int priority() {
    return Integer.MAX_VALUE;
  }

}
