package org.pitest.mutationtest.instrument;

import java.util.Collection;

import org.pitest.Description;
import org.pitest.MetaData;
import org.pitest.extension.ResultCollector;
import org.pitest.mutationtest.results.MutationResult;
import org.pitest.testunit.AbstractTestUnit;

public class KnownStatusMutationTestUnit extends AbstractTestUnit {
  
  private final Collection<MutationResult> mutations;
  private final Collection<String> mutatorNames;

  public KnownStatusMutationTestUnit(Collection<String> mutatorNames, final Collection<MutationResult> mutations) {
    super(new Description("Mutation test"));
    this.mutations = mutations;
    this.mutatorNames = mutatorNames;
  }

  @Override
  public void execute(ClassLoader loader, ResultCollector rc) {
    try {
      rc.notifyStart(this.getDescription());
      reportResults(rc);   
    } catch (final Throwable ex) {
      rc.notifyEnd(this.getDescription(), ex);
    }
    
  }

  private void reportResults(ResultCollector rc) {
    final MetaData md = new MutationMetaData(mutatorNames,
    mutations);
    rc.notifyEnd(this.getDescription(), md);
  }

}
