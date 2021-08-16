package org.pitest.mutationtest.engine.gregor.blocks;

public class ConcreteBlockCounter implements BlockCounter {

  private int     currentBlock = 0;
  private int     currentBlockThisMethod = 0;

  @Override
  public void registerNewBlock() {
    this.currentBlock++;
    this.currentBlockThisMethod++;
  }

  @Override
  public void registerNewMethodStart() {
    currentBlockThisMethod = 0;
  }

  public int getCurrentBlock() {
    return this.currentBlock;
  }

  public int getCurrentBlockThisMethod() {
    return this.currentBlockThisMethod;
  }

}
