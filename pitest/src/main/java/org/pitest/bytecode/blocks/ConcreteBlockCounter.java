package org.pitest.bytecode.blocks;

public class ConcreteBlockCounter implements BlockCounter {
  
  private int currentBlock = 0;
  private boolean isWithinExceptionHandler;

  public void registerNewBlock() {
    this.currentBlock++; 
  }

  public void registerFinallyBlockStart() {
    isWithinExceptionHandler = true; 
  }
  
  public void registerFinallyBlockEnd() {
    isWithinExceptionHandler = false; 
  }
  
  public int getCurrentBlock() {
    return currentBlock;
  }
  
  public boolean isWithinExceptionHandler() {
    return isWithinExceptionHandler;
  }

}
