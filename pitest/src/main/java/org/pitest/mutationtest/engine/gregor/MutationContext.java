package org.pitest.mutationtest.engine.gregor;

import java.util.Collection;

import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.blocks.BlockCounter;

public interface MutationContext extends BlockCounter {

  Collection<MutationDetails> getCollectedMutations();

  void registerCurrentLine(int line);

  ClassInfo getClassInfo();

  MutationIdentifier registerMutation(MethodMutatorFactory factory,
      String description);

  boolean shouldMutate(MutationIdentifier newId);

  void disableMutations(String reason);

  void enableMutatations(String reason);

}