package org.pitest.mutationtest.filter.support;

import org.pitest.classpath.CodeSource;
import org.pitest.mutationtest.engine.MutationDetails;

public interface MutationOnSwitchInstructionLookup {

  boolean isMutationOnSwitchInstruction(MutationDetails mutation,
      CodeSource source);
}
