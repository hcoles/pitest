package org.pitest.mutationtest.engine.gregor.analysis;


public class DefaultInstructionCounter implements InstructionCounter {

  private int count;

  public void increment() {
    this.count++;
  }

  public int currentInstructionCount() {
    return this.count;
  }

}
