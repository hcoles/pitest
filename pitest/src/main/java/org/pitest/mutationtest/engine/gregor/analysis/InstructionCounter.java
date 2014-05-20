package org.pitest.mutationtest.engine.gregor.analysis;

public interface InstructionCounter {

  public void increment();

  public int currentInstructionCount();

}
