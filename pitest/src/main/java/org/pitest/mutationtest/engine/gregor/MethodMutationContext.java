package org.pitest.mutationtest.engine.gregor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.pitest.functional.FunctionalList;
import org.pitest.functional.MutableList;
import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;

class MethodMutationContext implements MutationContext {

  private final ClassContext                    classContext;
  private final Location                        location;

  private final Map<String, Integer>            mutatorIndexes                 = new HashMap<String, Integer>();
  private final FunctionalList<MutationDetails> mutations                      = new MutableList<MutationDetails>();

  private int                                   lastLineNumber;
  private final Set<String>                     mutationFindingDisabledReasons = new HashSet<String>();

  MethodMutationContext(final ClassContext classContext, final Location location) {
    this.classContext = classContext;
    this.location = location;
  }

  public MutationIdentifier registerMutation(
      final MethodMutatorFactory factory, final String description) {
    final MutationIdentifier newId = getNextMutationIdentifer(factory,
        this.classContext.getJavaClassName());
    final MutationDetails details = new MutationDetails(newId,
        this.classContext.getFileName(), description, this.lastLineNumber,
        this.classContext.getCurrentBlock(),
        this.classContext.isWithinFinallyBlock(), false);
    registerMutation(details);
    return newId;
  }

  private MutationIdentifier getNextMutationIdentifer(
      final MethodMutatorFactory factory, final String className) {
    final int index = getAndIncrementIndex(factory);
    return new MutationIdentifier(this.location, index,
        factory.getGloballyUniqueId());
  }

  private int getAndIncrementIndex(final MethodMutatorFactory factory) {
    Integer index = this.mutatorIndexes.get(factory.getGloballyUniqueId());
    if (index == null) {
      index = 0;
    }
    this.mutatorIndexes.put(factory.getGloballyUniqueId(), (index + 1));
    return index;

  }

  private void registerMutation(final MutationDetails details) {
    if (!isMutationFindingDisabled()) {
      this.classContext.addMutation(details);
    }
  }

  private boolean isMutationFindingDisabled() {
    return !this.mutationFindingDisabledReasons.isEmpty();
  }

  public void registerCurrentLine(final int line) {
    this.lastLineNumber = line;
  }

  public Collection<MutationDetails> getCollectedMutations() {
    return this.mutations;
  }

  public void registerNewBlock() {
    this.classContext.registerNewBlock();

  }

  public void registerFinallyBlockStart() {
    this.classContext.registerFinallyBlockStart();
  }

  public void registerFinallyBlockEnd() {
    this.classContext.registerFinallyBlockEnd();
  }

  public ClassInfo getClassInfo() {
    return this.classContext.getClassInfo();
  }

  public boolean shouldMutate(final MutationIdentifier newId) {
    return this.classContext.shouldMutate(newId);
  }

  public void disableMutations(final String reason) {
    this.mutationFindingDisabledReasons.add(reason);
  }

  public void enableMutatations(final String reason) {
    this.mutationFindingDisabledReasons.remove(reason);
  }

}
