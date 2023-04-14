package org.pitest.mutationtest.engine.gregor;

import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.blocks.BlockCounter;

public interface MutationContext extends BasicContext, BlockCounter {

  void registerCurrentLine(int line);

  MutationIdentifier registerMutation(MethodMutatorFactory factory,
      String description);
}