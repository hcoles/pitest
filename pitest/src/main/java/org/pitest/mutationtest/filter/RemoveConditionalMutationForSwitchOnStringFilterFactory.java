package org.pitest.mutationtest.filter;

import java.util.Properties;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.filter.support.MutationOnSwitchInstructionLookupImpl;

public class RemoveConditionalMutationForSwitchOnStringFilterFactory
    implements MutationFilterFactory {

  @Override
  public MutationFilter createFilter(final Properties props,
      final CodeSource source, final int maxMutationsPerClass) {
    final MutationOnSwitchInstructionLookupImpl mutationOnSwitchInstructionLookup = new MutationOnSwitchInstructionLookupImpl();
    return new RemoveConditionalMutationForSwitchOnStringFilter(
        mutationOnSwitchInstructionLookup, source);
  }

  @Override
  public String description() {
    return "Remove Conditional Mutation for Switch On String Filter";
  }
}
