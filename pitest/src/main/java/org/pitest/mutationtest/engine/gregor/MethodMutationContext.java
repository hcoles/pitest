package org.pitest.mutationtest.engine.gregor;

import org.pitest.mutationtest.engine.Location;
import org.pitest.mutationtest.engine.MutationDetails;
import org.pitest.mutationtest.engine.MutationIdentifier;
import org.pitest.mutationtest.engine.gregor.analysis.InstructionCounter;

class MethodMutationContext implements MutationContext, InstructionCounter {

  private final ClassContext classContext;
  private final Location     location;

  private int                instructionIndex;

  private int                lastLineNumber;

  MethodMutationContext(final ClassContext classContext, final Location location) {
    this.classContext = classContext;
    this.location = location;
  }

  @Override
  public MutationIdentifier registerMutation(
      final MethodMutatorFactory factory, final String description) {
    final MutationIdentifier newId = getNextMutationIdentifier(factory);
    registerMutation(newId, description);
    return newId;
  }

  @Override
  public void registerMutation(MutationIdentifier id, String description) {
    final MutationDetails details = new MutationDetails(id,
            this.classContext.getFileName(), description, this.lastLineNumber,
            this.classContext.getCurrentBlock());
    registerMutation(details);
  }

  private MutationIdentifier getNextMutationIdentifier(
      final MethodMutatorFactory factory) {
    return new MutationIdentifier(this.location, this.instructionIndex,
        factory.getGloballyUniqueId());
  }

  private void registerMutation(final MutationDetails details) {
      this.classContext.addMutation(details);
  }

  @Override
  public void registerCurrentLine(final int line) {
    this.lastLineNumber = line;
  }

  @Override
  public void registerNewBlock() {
    this.classContext.registerNewBlock();
  }

  @Override
  public void registerNewMethodStart() {
    this.classContext.registerNewMethodStart();
  }

  @Override
  public ClassInfo getClassInfo() {
    return this.classContext.getClassInfo();
  }

  @Override
  public boolean shouldMutate(final MutationIdentifier newId) {
    return this.classContext.shouldMutate(newId);
  }

  @Override
  public void increment() {
    this.instructionIndex = this.instructionIndex + 1;

  }

  @Override
  public int currentInstructionCount() {
    return this.instructionIndex;
  }

}
