package org.pitest.mutationtest.engine.gregor.blocks;

public class ConcreteBlockCounter implements BlockCounter {

  private int     currentBlock = 0;
  private boolean isWithinExceptionHandler;

  public void registerNewBlock() {
    this.currentBlock++;
  }

  public void registerFinallyBlockStart() {
    this.isWithinExceptionHandler = true;
  }

  public void registerFinallyBlockEnd() {
    this.isWithinExceptionHandler = false;
  }

  public int getCurrentBlock() {
    return this.currentBlock;
  }

  public boolean isWithinFinallyBlock() {
    return this.isWithinExceptionHandler;
  }

}
